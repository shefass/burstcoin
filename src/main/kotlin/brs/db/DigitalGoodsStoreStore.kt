package brs.db

import brs.entity.Goods
import brs.entity.Purchase
import burst.kit.entity.BurstEncryptedMessage

interface DigitalGoodsStoreStore {
    /**
     * TODO
     */
    val feedbackDbKeyFactory: BurstKey.LongKeyFactory<Purchase>

    /**
     * TODO
     */
    val purchaseDbKeyFactory: BurstKey.LongKeyFactory<Purchase>

    /**
     * TODO
     */
    val purchaseTable: MutableEntityTable<Purchase>

    /**
     * TODO
     */
    val feedbackTable: ValuesTable<Purchase, BurstEncryptedMessage>

    /**
     * TODO
     */
    val publicFeedbackDbKeyFactory: BurstKey.LongKeyFactory<Purchase>

    /**
     * TODO
     */
    val publicFeedbackTable: ValuesTable<Purchase, String>

    /**
     * TODO
     */
    val goodsDbKeyFactory: BurstKey.LongKeyFactory<Goods>

    /**
     * TODO
     */
    val goodsTable: MutableEntityTable<Goods>

    /**
     * TODO
     */
    fun getGoodsInStock(from: Int, to: Int): Collection<Goods>

    /**
     * TODO
     */
    fun getSellerGoods(sellerId: Long, inStockOnly: Boolean, from: Int, to: Int): Collection<Goods>

    /**
     * TODO
     */
    fun getAllPurchases(from: Int, to: Int): Collection<Purchase>

    /**
     * TODO
     */
    fun getSellerPurchases(sellerId: Long, from: Int, to: Int): Collection<Purchase>

    /**
     * TODO
     */
    fun getBuyerPurchases(buyerId: Long, from: Int, to: Int): Collection<Purchase>

    /**
     * TODO
     */
    fun getSellerBuyerPurchases(sellerId: Long, buyerId: Long, from: Int, to: Int): Collection<Purchase>

    /**
     * TODO
     */
    fun getPendingSellerPurchases(sellerId: Long, from: Int, to: Int): Collection<Purchase>

    /**
     * TODO
     */
    fun getExpiredPendingPurchases(timestamp: Int): Collection<Purchase>
}
