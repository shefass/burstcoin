package brs.db.sql

import brs.db.BurstKey
import brs.db.TradeStore
import brs.db.useDslContext
import brs.entity.DependencyProvider
import brs.entity.Trade
import brs.schema.Tables.TRADE
import org.jooq.DSLContext
import org.jooq.Record

internal class SqlTradeStore(private val dp: DependencyProvider) : TradeStore {
    override val tradeDbKeyFactory: SqlDbKey.LinkKeyFactory<Trade> =
        object : SqlDbKey.LinkKeyFactory<Trade>(TRADE.ASK_ORDER_ID, TRADE.BID_ORDER_ID) {
            override fun newKey(entity: Trade): BurstKey {
                return entity.dbKey
            }
        }

    override val tradeTable: SqlEntityTable<Trade>

    init {
        tradeTable = object : SqlEntityTable<Trade>(TRADE, TRADE.HEIGHT, null, tradeDbKeyFactory, dp) {
            override fun load(record: Record) = Trade(
                record.get(TRADE.TIMESTAMP),
                record.get(TRADE.ASSET_ID),
                record.get(TRADE.BLOCK_ID),
                record.get(TRADE.HEIGHT),
                record.get(TRADE.ASK_ORDER_ID),
                record.get(TRADE.BID_ORDER_ID),
                record.get(TRADE.ASK_ORDER_HEIGHT),
                record.get(TRADE.BID_ORDER_HEIGHT),
                record.get(TRADE.SELLER_ID),
                record.get(TRADE.BUYER_ID),
                tradeDbKeyFactory.newKey(record.get(TRADE.ASK_ORDER_ID), record.get(TRADE.BID_ORDER_ID)),
                record.get(TRADE.QUANTITY),
                record.get(TRADE.PRICE))

            override fun save(ctx: DSLContext, entity: Trade) {
                ctx.insertInto(
                        TRADE,
                        TRADE.ASSET_ID,
                        TRADE.BLOCK_ID,
                        TRADE.ASK_ORDER_ID,
                        TRADE.BID_ORDER_ID,
                        TRADE.ASK_ORDER_HEIGHT,
                        TRADE.BID_ORDER_HEIGHT,
                        TRADE.SELLER_ID,
                        TRADE.BUYER_ID,
                        TRADE.QUANTITY,
                        TRADE.PRICE,
                        TRADE.TIMESTAMP,
                        TRADE.HEIGHT
                    )
                    .values(
                        entity.assetId,
                        entity.blockId,
                        entity.askOrderId,
                        entity.bidOrderId,
                        entity.askOrderHeight,
                        entity.bidOrderHeight,
                        entity.sellerId,
                        entity.buyerId,
                        entity.quantity,
                        entity.pricePlanck,
                        entity.timestamp,
                        entity.height
                    )
                    .execute()
            }

            override fun save(ctx: DSLContext, entities: Collection<Trade>) {
                if (entities.isEmpty()) return
                val query = ctx.insertInto(
                    TRADE,
                    TRADE.ASSET_ID,
                    TRADE.BLOCK_ID,
                    TRADE.ASK_ORDER_ID,
                    TRADE.BID_ORDER_ID,
                    TRADE.ASK_ORDER_HEIGHT,
                    TRADE.BID_ORDER_HEIGHT,
                    TRADE.SELLER_ID,
                    TRADE.BUYER_ID,
                    TRADE.QUANTITY,
                    TRADE.PRICE,
                    TRADE.TIMESTAMP,
                    TRADE.HEIGHT
                )
                entities.forEach { entity ->
                    query.values(
                        entity.assetId,
                        entity.blockId,
                        entity.askOrderId,
                        entity.bidOrderId,
                        entity.askOrderHeight,
                        entity.bidOrderHeight,
                        entity.sellerId,
                        entity.buyerId,
                        entity.quantity,
                        entity.pricePlanck,
                        entity.timestamp,
                        entity.height
                    )
                }
                query.execute()
            }
        }
    }

    override fun getAllTrades(from: Int, to: Int): Collection<Trade> {
        return tradeTable.getAll(from, to)
    }

    override fun getAssetTrades(assetId: Long, from: Int, to: Int): Collection<Trade> {
        return tradeTable.getManyBy(TRADE.ASSET_ID.eq(assetId), from, to)
    }

    override fun getAccountTrades(accountId: Long, from: Int, to: Int): Collection<Trade> {
        return dp.db.useDslContext { ctx ->
            val selectQuery = ctx
                .selectFrom(TRADE).where(
                    TRADE.SELLER_ID.eq(accountId)
                )
                .unionAll(
                    ctx.selectFrom(TRADE).where(
                        TRADE.BUYER_ID.eq(accountId).and(
                            TRADE.SELLER_ID.ne(accountId)
                        )
                    )
                )
                .orderBy(TRADE.HEIGHT.desc())
                .query
            SqlDbUtils.applyLimits(selectQuery, from, to)

            tradeTable.getManyBy(ctx, selectQuery, false)
        }
    }

    override fun getAccountAssetTrades(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Trade> {
        return dp.db.useDslContext { ctx ->
            val selectQuery = ctx
                .selectFrom(TRADE).where(
                    TRADE.SELLER_ID.eq(accountId).and(TRADE.ASSET_ID.eq(assetId))
                )
                .unionAll(
                    ctx.selectFrom(TRADE).where(
                        TRADE.BUYER_ID.eq(accountId)
                    ).and(
                        TRADE.SELLER_ID.ne(accountId)
                    ).and(TRADE.ASSET_ID.eq(assetId))
                )
                .orderBy(TRADE.HEIGHT.desc())
                .query
            SqlDbUtils.applyLimits(selectQuery, from, to)

            tradeTable.getManyBy(ctx, selectQuery, false)
        }
    }

    override fun getTradeCount(assetId: Long): Int {
        return dp.db.useDslContext { ctx -> ctx.fetchCount(ctx.selectFrom(TRADE).where(TRADE.ASSET_ID.eq(assetId))) }
    }
}
