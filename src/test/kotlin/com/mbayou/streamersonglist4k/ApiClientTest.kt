package com.mbayou.streamersonglist4k

import com.mbayou.streamersonglist4k.api.ApiClient
import com.mbayou.streamersonglist4k.api.AttributeId
import com.mbayou.streamersonglist4k.api.SortDirection
import kotlin.test.Test
import kotlin.test.assertEquals

class ApiClientTest {
    @Test
    fun `buildUrl encodes value classes enums arrays and repeated list params`() {
        val url = ApiClient.buildUrl(
            "https://api.staging.streamersonglist.com/songs",
            mapOf(
                "attributes" to listOf(AttributeId(10), AttributeId(20)),
                "order_dir" to SortDirection.DESC,
                "search" to "hello world",
                "ignored" to null,
            )
        )

        assertEquals(
            "https://api.staging.streamersonglist.com/songs?attributes=10&attributes=20&order_dir=desc&search=hello+world",
            url
        )
    }
}
