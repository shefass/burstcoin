package brs.api.grpc.api

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.ApiException
import brs.api.grpc.ProtoBuilder
import brs.services.AliasService

class GetAliasHandler(private val aliasService: AliasService) : GrpcApiHandler<BrsApi.GetAliasRequest, BrsApi.Alias> {
    override fun handleRequest(request: BrsApi.GetAliasRequest): BrsApi.Alias {
        val alias =
            (if (request.name.isEmpty()) aliasService.getAlias(request.id) else aliasService.getAlias(request.name))
                ?: throw ApiException(
                    "Alias not found"
                )
        return ProtoBuilder.buildAlias(alias, aliasService.getOffer(alias))
    }
}
