package brs.api.http

import brs.api.http.common.ResultFields.DELISTED_RESPONSE
import brs.api.http.common.ResultFields.DESCRIPTION_RESPONSE
import brs.api.http.common.ResultFields.GOODS_RESPONSE
import brs.api.http.common.ResultFields.NAME_RESPONSE
import brs.api.http.common.ResultFields.PRICE_PLANCK_RESPONSE
import brs.api.http.common.ResultFields.QUANTITY_RESPONSE
import brs.api.http.common.ResultFields.TAGS_RESPONSE
import brs.api.http.common.ResultFields.TIMESTAMP_RESPONSE
import brs.common.QuickMocker
import brs.entity.Goods
import brs.services.ParameterService
import brs.util.json.getMemberAsBoolean
import brs.util.json.getMemberAsLong
import brs.util.json.getMemberAsString
import com.google.gson.JsonObject
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetDGSGoodTest {
    private lateinit var t: GetDGSGood

    private lateinit var mockParameterService: ParameterService

    @Before
    fun setUp() {
        mockParameterService = mockk(relaxed = true)

        t = GetDGSGood(mockParameterService)
    }

    @Test
    fun processRequest() {
        val mockGoods = mockk<Goods>(relaxed = true)
        every { mockGoods.id } returns 1L
        every { mockGoods.name } returns "name"
        every { mockGoods.description } returns "description"
        every { mockGoods.quantity } returns 2
        every { mockGoods.pricePlanck } returns 3L
        every { mockGoods.tags } returns "tags"
        every { mockGoods.isDelisted } returns true
        every { mockGoods.timestamp } returns 12345

        val request = QuickMocker.httpServletRequest()

        every { mockParameterService.getGoods(eq(request)) } returns mockGoods

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        assertEquals(mockGoods.id.toString(), result.getMemberAsString(GOODS_RESPONSE))
        assertEquals(mockGoods.name, result.getMemberAsString(NAME_RESPONSE))
        assertEquals(mockGoods.description, result.getMemberAsString(DESCRIPTION_RESPONSE))
        assertEquals(mockGoods.quantity.toLong(), result.getMemberAsLong(QUANTITY_RESPONSE))
        assertEquals(mockGoods.pricePlanck.toString(), result.getMemberAsString(PRICE_PLANCK_RESPONSE))
        assertEquals(mockGoods.tags, result.getMemberAsString(TAGS_RESPONSE))
        assertEquals(mockGoods.isDelisted, result.getMemberAsBoolean(DELISTED_RESPONSE))
        assertEquals(mockGoods.timestamp.toLong(), result.getMemberAsLong(TIMESTAMP_RESPONSE))
    }
}