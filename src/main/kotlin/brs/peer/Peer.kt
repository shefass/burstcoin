package brs.peer

import brs.entity.Block
import brs.entity.PeerInfo
import brs.entity.Transaction
import brs.util.Version
import java.math.BigInteger

interface Peer {
    /**
     * The address that this peer has contacted us from
     */
    val remoteAddress: String

    /**
     * The address this peer has announced as being contactable at. If the peer did not announce an address, this will be null, and the peer cannot be contacted.
     */
    val announcedAddress: PeerAddress?

    /**
     * Updates the peer's address and disconnects to force re-verification of new address.
     */
    fun updateAddress(newAnnouncedAddress: PeerAddress)

    /**
     * Whether the peer is currently connected
     */
    val isConnected: Boolean

    var version: Version

    var application: String

    /**
     * Peer operator's "platform" string theoretically describing what platform they are running on
     */
    var platform: String

    val isBlacklisted: Boolean

    var lastHandshakeTime: Int

    /**
     * Connect to the peer, and handshake.
     * @return whether the connection was successful
     */
    fun connect(): Boolean

    fun disconnect()

    fun isHigherOrEqualVersionThan(version: Version): Boolean

    fun blacklist(cause: Exception, description: String)

    fun blacklist(description: String)

    fun updateBlacklistedStatus(curTime: Long)

    /**
     * Send the peer our [PeerInfo] and returns theirs
     * @throws Exception if unsuccessful
     */
    fun exchangeInfo(): PeerInfo?

    /**
     * Get the peer's cumulative difficulty and current blockchain height
     * @return A pair with first value of the Cumulative Difficulty and second value of the Blockchain Height, or null if unsuccessful
     */
    fun getCumulativeDifficulty(): Pair<BigInteger, Int>?

    /**
     * Get any unconfirmed transactions the peer has for us
     * @return A list of unconfirmed transactions, or null if unsuccessful
     */
    fun getUnconfirmedTransactions(): Collection<Transaction>?

    /**
     * TODO improve doc
     * Get milestone block IDs from a peer since the last block ID in the download cache
     * @return A pair with first value of the milestone block IDs and second value being whether this is the last block ID, or null if unsuccessful
     */
    fun getMilestoneBlockIds(): Pair<Collection<Long>, Boolean>?

    /**
     * TODO improve doc
     * Get milestone block IDs from a peer since [lastMilestoneBlockId]
     * @return A pair with first value of the milestone block IDs and second value being whether this is the last block ID, or null if unsuccessful
     */
    fun getMilestoneBlockIds(lastMilestoneBlockId: Long): Pair<Collection<Long>, Boolean>?

    /**
     * Sends the unconfirmed transactions to the peer.
     * Fails silently.
     * limited to the first [brs.objects.Constants.MAX_PEER_RECEIVED_BLOCKS] that the peer returns
     */
    fun sendUnconfirmedTransactions(transactions: Collection<Transaction>)

    /**
     * Gets the blocks after [lastBlockId] from the peer,
     * limited to the first [brs.objects.Constants.MAX_PEER_RECEIVED_BLOCKS] that the peer returns
     * @reutrns the blocks returned by the peer, or null if unsuccessful
     */
    fun getNextBlocks(lastBlockId: Long): Collection<Block>?

    /**
     * Gets the block IDs after [lastBlockId] from the peer, or null if unsuccessful
     */
    fun getNextBlockIds(lastBlockId: Long): Collection<Long>?

    /**
     * Notifies this peer of the other peers
     * Fails silently.
     * @param announcedAddresses The announced addresses to notify this peer of
     */
    fun addPeers(announcedAddresses: Collection<PeerAddress>)

    /**
     * Get new peer addresses from this peer, or null if unsuccessful.
     * The peer can send us as many addresses as it wants TODO
     * It is not guaranteed that we do not already know of the peers that the peer sends us.
     */
    fun getPeers(): Collection<PeerAddress>?

    /**
     * Sends [block] to the peer to be added
     * @return Whether the peer accepted the [block], or false if unsuccessful
     */
    fun sendBlock(block: Block): Boolean

    var shareAddress: Boolean

    companion object {
        fun isHigherOrEqualVersion(ourVersion: Version?, possiblyLowerVersion: Version?): Boolean {
            return ourVersion != null && possiblyLowerVersion?.isGreaterThanOrEqualTo(ourVersion) ?: false
        }
    }
}
