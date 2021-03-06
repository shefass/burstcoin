package brs.db.sql

import brs.db.PeerDb
import brs.db.ensureInTransaction
import brs.db.useDslContext
import brs.entity.DependencyProvider
import brs.schema.Tables.PEER

internal class SqlPeerDb(private val dp: DependencyProvider) : PeerDb {
    override fun loadPeers(): List<String> {
        return dp.db.useDslContext<List<String>> { ctx ->
            ctx.selectFrom(PEER).fetch(PEER.ADDRESS, String::class.java)
        }
    }

    override fun updatePeers(peers: List<String>) {
        dp.db.ensureInTransaction {
            dp.db.useDslContext { ctx ->
                val dbPeers = ctx.selectFrom(PEER).fetch(PEER.ADDRESS)
                ctx.deleteFrom(PEER).where(PEER.ADDRESS.notIn(peers))
                ctx.batch(peers.mapNotNull {
                    if (dbPeers.contains(it)) null else ctx.insertInto(PEER).set(PEER.ADDRESS,
                        it)
                })
            }
        }
    }

    override fun optimize() {
        dp.db.optimizeTable(PEER.name)
    }
}
