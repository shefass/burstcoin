package brs.db.sql

import brs.db.*
import brs.entity.DependencyProvider
import brs.util.cache.set
import org.ehcache.Cache
import org.jooq.*
import org.jooq.Table

internal abstract class SqlBatchEntityTable<T> internal constructor(
    table: Table<*>,
    dbKeyFactory: SqlDbKey.Factory<T>,
    heightField: Field<Int>,
    override val cacheValueClass: Class<T>,
    private val dp: DependencyProvider
) : SqlEntityTable<T>(table, heightField, null, dbKeyFactory, dp), BatchTable, CachedTable<SqlDbKey, T> {
    override val count: Int
        get() {
            assertNotInTransaction()
            return super.count
        }

    override val rowCount: Int
        get() {
            assertNotInTransaction()
            return super.rowCount
        }

    override val cacheKeyClass = SqlDbKey::class.java
    override val cacheName: String = table.name

    private var lastFinishHeight: Int = -1

    @Suppress("UNCHECKED_CAST")
    private val batch: MutableMap<SqlDbKey, T>
        get() = dp.db.getBatch<T>(table) as MutableMap<SqlDbKey, T>

    private val batchCache: Cache<SqlDbKey, T> get() = getCache(dp)

    private fun assertNotInTransaction() {
        check(!dp.db.isInTransaction()) { "Cannot use batch table during transaction" }
    }

    protected abstract fun saveBatch(ctx: DSLContext, entities: Collection<T>)

    final override fun save(ctx: DSLContext, entity: T) {
        insert(entity)
    }

    final override fun save(ctx: DSLContext, entities: Collection<T>) {
        if (entities.isEmpty()) return
        entities.forEach { insert(it) }
    }

    override fun get(dbKey: BurstKey): T? {
        require(dbKey is SqlDbKey)
        if (batchCache.containsKey(dbKey)) {
            return batchCache.get(dbKey)
        } else if (dp.db.isInTransaction() && batch.containsKey(dbKey)) {
            return batch[dbKey]
        }
        val item = super.get(dbKey)
        if (item != null) {
            batchCache[dbKey] = item
        }
        return item
    }

    override fun insert(entity: T) {
        dp.db.assertInTransaction()
        val key = dbKeyFactory.newKey(entity)
        check(key is SqlDbKey)
        batch[key] = entity
        batchCache[key] = entity
    }

    override fun flushBatch(height: Int) {
        dp.db.assertInTransaction()
        if (batch.isNotEmpty()) {
            require(height != lastFinishHeight) { "Already finished block height $height and batch is not empty" }
            dp.db.useDslContext { ctx ->
                saveBatch(ctx, batch.values)
                batch.clear()
                lastFinishHeight = height
            }
        }
    }

    override fun get(dbKey: BurstKey, height: Int): T? {
        assertNotInTransaction()
        return super.get(dbKey, height)
    }

    override fun getBy(condition: Condition): T? {
        assertNotInTransaction()
        return super.getBy(condition)
    }

    override fun getBy(condition: Condition, height: Int): T? {
        assertNotInTransaction()
        return super.getBy(condition, height)
    }

    override fun getManyBy(condition: Condition, from: Int, to: Int, sort: Collection<SortField<*>>): Collection<T> {
        assertNotInTransaction()
        return super.getManyBy(condition, from, to, sort)
    }

    override fun getManyBy(condition: Condition, height: Int, from: Int, to: Int, sort: Collection<SortField<*>>): Collection<T> {
        assertNotInTransaction()
        return super.getManyBy(condition, height, from, to, sort)
    }

    override fun getManyBy(ctx: DSLContext, query: SelectQuery<out Record>, cache: Boolean): Collection<T> {
        assertNotInTransaction()
        return super.getManyBy(ctx, query, cache)
    }

    override fun getAll(from: Int, to: Int, sort: Collection<SortField<*>>): Collection<T> {
        assertNotInTransaction()
        return super.getAll(from, to, sort)
    }

    override fun getAll(height: Int, from: Int, to: Int, sort: Collection<SortField<*>>): Collection<T> {
        assertNotInTransaction()
        return super.getAll(height, from, to, sort)
    }

    override fun rollback(height: Int) {
        super.rollback(height)
        batch.clear()
        lastFinishHeight = -1
    }
}
