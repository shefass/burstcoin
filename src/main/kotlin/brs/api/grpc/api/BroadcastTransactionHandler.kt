package brs.api.grpc.api

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.ProtoBuilder
import brs.entity.DependencyProvider

class BroadcastTransactionHandler(private val dp: DependencyProvider) :
    GrpcApiHandler<BrsApi.BasicTransaction, BrsApi.TransactionBroadcastResult> {
    override fun handleRequest(request: BrsApi.BasicTransaction): BrsApi.TransactionBroadcastResult {
        return BrsApi.TransactionBroadcastResult.newBuilder()
            .setNumberOfPeersSentTo(
                dp.transactionProcessorService.broadcast(
                    ProtoBuilder.parseBasicTransaction(
                        dp,
                        request
                    )
                )!!
            )
            .build()
    }
}
