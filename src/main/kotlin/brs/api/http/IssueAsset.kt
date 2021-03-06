package brs.api.http

import brs.api.http.common.JSONResponses.INCORRECT_ASSET_DESCRIPTION
import brs.api.http.common.JSONResponses.INCORRECT_ASSET_NAME
import brs.api.http.common.JSONResponses.INCORRECT_ASSET_NAME_LENGTH
import brs.api.http.common.JSONResponses.INCORRECT_DECIMALS
import brs.api.http.common.JSONResponses.MISSING_NAME
import brs.api.http.common.Parameters.DECIMALS_PARAMETER
import brs.api.http.common.Parameters.DESCRIPTION_PARAMETER
import brs.api.http.common.Parameters.NAME_PARAMETER
import brs.api.http.common.Parameters.QUANTITY_QNT_PARAMETER
import brs.entity.DependencyProvider
import brs.objects.Constants
import brs.transaction.appendix.Attachment
import brs.util.convert.emptyToNull
import brs.util.jetty.get
import brs.util.string.isInAlphabet
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class IssueAsset internal constructor(private val dp: DependencyProvider) : CreateTransaction(
    dp,
    arrayOf(APITag.AE, APITag.CREATE_TRANSACTION),
    NAME_PARAMETER,
    DESCRIPTION_PARAMETER,
    QUANTITY_QNT_PARAMETER,
    DECIMALS_PARAMETER
) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        var name = request[NAME_PARAMETER].emptyToNull()
        val description = request[DESCRIPTION_PARAMETER].emptyToNull()
        val decimalsValue = request[DECIMALS_PARAMETER].emptyToNull()

        if (name == null) {
            return MISSING_NAME
        }

        name = name.trim()
        if (name.length < Constants.MIN_ASSET_NAME_LENGTH || name.length > Constants.MAX_ASSET_NAME_LENGTH) {
            return INCORRECT_ASSET_NAME_LENGTH
        }

        if (!name.isInAlphabet()) {
            return INCORRECT_ASSET_NAME
        }

        if (description != null && description.length > Constants.MAX_ASSET_DESCRIPTION_LENGTH) {
            return INCORRECT_ASSET_DESCRIPTION
        }

        var decimals: Byte = 0
        if (decimalsValue != null) {
            try {
                decimals = decimalsValue.toByte()
                if (decimals < 0 || decimals > 8) {
                    return INCORRECT_DECIMALS
                }
            } catch (e: NumberFormatException) {
                return INCORRECT_DECIMALS
            }
        }

        val quantity = ParameterParser.getQuantity(request)
        val account = dp.parameterService.getSenderAccount(request)
        val attachment = Attachment.ColoredCoinsAssetIssuance(
            dp,
            name,
            description!!,
            quantity,
            decimals,
            dp.blockchainService.height
        )
        return createTransaction(request, account, attachment)
    }
}
