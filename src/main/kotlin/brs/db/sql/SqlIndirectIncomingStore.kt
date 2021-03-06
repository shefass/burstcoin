package brs.db.sql

import brs.db.BurstKey
import brs.db.IndirectIncomingStore
import brs.db.useDslContext
import brs.entity.DependencyProvider
import brs.entity.IndirectIncoming
import brs.schema.Tables.INDIRECT_INCOMING
import org.jooq.DSLContext
import org.jooq.Record

internal class SqlIndirectIncomingStore(private val dp: DependencyProvider) : IndirectIncomingStore {
    internal val indirectIncomingTable: SqlEntityTable<IndirectIncoming>

    init {
        val indirectIncomingDbKeyFactory =
            object : SqlDbKey.LinkKeyFactory<IndirectIncoming>(INDIRECT_INCOMING.ACCOUNT_ID, INDIRECT_INCOMING.TRANSACTION_ID) {
                override fun newKey(entity: IndirectIncoming): BurstKey {
                    return newKey(entity.accountId, entity.transactionId)
                }
            }

        this.indirectIncomingTable = object : SqlBatchEntityTable<IndirectIncoming>(INDIRECT_INCOMING, indirectIncomingDbKeyFactory, INDIRECT_INCOMING.HEIGHT, IndirectIncoming::class.java, dp) {
            override fun load(record: Record): IndirectIncoming {
                return IndirectIncoming(
                    record.get(INDIRECT_INCOMING.ACCOUNT_ID),
                    record.get(INDIRECT_INCOMING.TRANSACTION_ID),
                    record.get(INDIRECT_INCOMING.HEIGHT)
                )
            }

            override fun saveBatch(ctx: DSLContext, entities: Collection<IndirectIncoming>) {
                val query = ctx.insertInto(INDIRECT_INCOMING, INDIRECT_INCOMING.ACCOUNT_ID, INDIRECT_INCOMING.TRANSACTION_ID, INDIRECT_INCOMING.HEIGHT)
                entities.forEach { (accountId, transactionId, height) ->
                    query.values(accountId, transactionId, height)
                }
                query.execute()
            }
        }
    }

    override fun addIndirectIncomings(indirectIncomings: Collection<IndirectIncoming>) {
        dp.db.useDslContext { ctx -> indirectIncomingTable.save(ctx, indirectIncomings) }
    }

    override fun getIndirectIncomings(accountId: Long, from: Int, to: Int): List<Long> {
        return indirectIncomingTable.getManyBy(INDIRECT_INCOMING.ACCOUNT_ID.eq(accountId), from, to)
            .map { it.transactionId }
    }
}
