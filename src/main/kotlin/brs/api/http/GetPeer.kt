package brs.api.http

import brs.api.http.common.JSONData
import brs.api.http.common.JSONResponses.MISSING_PEER
import brs.api.http.common.JSONResponses.UNKNOWN_PEER
import brs.api.http.common.Parameters.PEER_PARAMETER
import brs.entity.DependencyProvider
import brs.util.jetty.get
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetPeer(private val dp: DependencyProvider) :
    APIServlet.JsonRequestHandler(arrayOf(APITag.INFO), PEER_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val peerAddress = request[PEER_PARAMETER] ?: return MISSING_PEER
        val peer = dp.peerService.getPeer(peerAddress) ?: return UNKNOWN_PEER
        return JSONData.peer(peer)
    }
}
