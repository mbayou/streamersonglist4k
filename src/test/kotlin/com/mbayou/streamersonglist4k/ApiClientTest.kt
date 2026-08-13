package com.mbayou.streamersonglist4k

import com.mbayou.streamersonglist4k.api.ApiClient
import com.mbayou.streamersonglist4k.api.AttributeId
import com.mbayou.streamersonglist4k.api.SortDirection
import com.mbayou.streamersonglist4k.api.StreamerSonglistAuthentication
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import java.net.http.HttpClient
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

    @Test
    fun `oauth requests include the owning client id header`() {
        val httpClient = FakeHttpClient(statusCode = 200, body = "{}")
        val configuration = StreamerSonglistConfiguration.builder()
            .clientId("client-id")
            .clientSecret("client-secret")
            .redirectUri("https://app.example/callback")
            .build()
        val client = HeaderTestClient(httpClient, configuration)

        client.execute(StreamerSonglistAuthentication.OAuthAccessToken("access-token"))

        assertEquals("Bearer access-token", httpClient.lastRequest?.headers()?.firstValue("Authorization")?.orElse(null))
        assertEquals("client-id", httpClient.lastRequest?.headers()?.firstValue("Client-Id")?.orElse(null))
    }
}

private class HeaderTestClient(
    httpClient: HttpClient,
    configuration: StreamerSonglistConfiguration,
) : ApiClient(httpClient, ObjectMapper(), configuration, null) {
    fun execute(authentication: StreamerSonglistAuthentication) {
        get("/test")
            .authentication(authentication)
            .send(object : TypeReference<Map<String, Any?>>() {})
    }
}
