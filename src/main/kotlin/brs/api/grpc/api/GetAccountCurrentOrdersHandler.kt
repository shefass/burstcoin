package brs.api.grpc.api

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.ApiException
import brs.api.grpc.ProtoBuilder
import brs.services.AssetExchangeService

class GetAccountCurrentOrdersHandler(private val assetExchangeService: AssetExchangeService) :
    GrpcApiHandler<BrsApi.GetAccountOrdersRequest, BrsApi.Orders> {
    override fun handleRequest(request: BrsApi.GetAccountOrdersRequest): BrsApi.Orders {
        val accountId = request.account
        val assetId = request.asset
        val indexRange = ProtoBuilder.sanitizeIndexRange(request.indexRange)
        val firstIndex = indexRange.firstIndex
        val lastIndex = indexRange.lastIndex

        val builder = BrsApi.Orders.newBuilder()
        when (request.orderType) {
            BrsApi.AssetOrderType.ASK -> (if (assetId == 0L) assetExchangeService.getAskOrdersByAccount(
                accountId,
                firstIndex,
                lastIndex
            ) else assetExchangeService.getAskOrdersByAccountAsset(accountId, assetId, firstIndex, lastIndex))
                .forEach { order -> builder.addOrders(ProtoBuilder.buildOrder(order)) }
            BrsApi.AssetOrderType.BID -> (if (assetId == 0L) assetExchangeService.getBidOrdersByAccount(
                accountId,
                firstIndex,
                lastIndex
            ) else assetExchangeService.getBidOrdersByAccountAsset(accountId, assetId, firstIndex, lastIndex))
                .forEach { order -> builder.addOrders(ProtoBuilder.buildOrder(order)) }
            else -> throw ApiException("Order Type not set")
        }
        return builder.build()
    }
}
