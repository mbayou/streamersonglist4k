package com.mbayou.streamersonglist4k

import com.mbayou.streamersonglist4k.api.*
import com.mbayou.streamersonglist4k.authorization.OAuthTokenError
import com.mbayou.streamersonglist4k.authorization.OAuthTokenException
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.time.Duration
import kotlin.test.*

class ProviderContractTest {
    private fun fixture(name: String) = requireNotNull(javaClass.getResource("/contract/$name.json")).readText()

    private fun withProvider(body: (String) -> String, test: (StreamerSonglistClient) -> Unit) {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { request ->
            val response = body(request.requestURI.path).toByteArray()
            request.requestBody.close()
            request.sendResponseHeaders(200, response.size.toLong())
            request.responseBody.use { it.write(response) }
        }
        server.start()
        try {
            val base = "http://127.0.0.1:${server.address.port}"
            test(StreamerSonglistClient(StreamerSonglistConfiguration.builder()
                .clientId("test-client").clientSecret("test-secret").redirectUri("https://example.com/callback")
                .apiBaseUrl(base).oAuthBaseUrl(base).build(), StreamerSonglistAuthentication.OAuthAccessToken("test-access")))
        } finally { server.stop(0) }
    }

    @Test
    fun `current profile without legacy fields resolves with the production mapper`() = withProvider({ fixture("streamer") }) { client ->
        val profile = client.streamers().getStreamer(StreamerLookup.ById(StreamerId(7)))
        assertEquals(StreamerId(7), profile.id)
        assertTrue(profile.requestsActive)
        assertEquals("test-streamer", profile.platforms.twitch?.username)
        assertFalse(profile.fixedTokenPrice)
    }

    @Test
    fun `current queue and mutation responses decode successfully`() = withProvider({ path ->
        when (path) {
            "/queue/42/requests/9" -> fixture("delete-request")
            "/play-history" -> fixture("play-history")
            else -> fixture("queue")
        }
    }) { client ->
        val snapshot = client.queue().getQueue(StreamerLookup.ById(StreamerId(7)))
        assertEquals("Playing song", snapshot.playing?.song?.title)
        assertEquals("Upcoming song", snapshot.items?.single()?.song?.title)
        assertTrue(client.queue().deleteRequest(QueueId(42), RequestId(9)).queueDeleted)
    }

    @Test
    fun `clear queue returns deletion count instead of obsolete message`() = withProvider({ fixture("clear-queue") }) { client ->
        assertEquals(2, client.queue().clearQueue(StreamerLookup.ById(StreamerId(7))).deleted)
    }

    @Test
    fun `OAuth errors retain typed code without including response secrets in exception text`() {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/token") { request ->
            val bytes = """{"error":"invalid_grant","refresh_token":"do-not-log"}""".toByteArray()
            request.sendResponseHeaders(400, bytes.size.toLong())
            request.responseBody.use { it.write(bytes) }
        }
        server.start()
        try {
            val client = StreamerSonglistClient(StreamerSonglistConfiguration.builder()
                .clientId("client").clientSecret("secret").redirectUri("https://example.com")
                .oAuthBaseUrl("http://127.0.0.1:${server.address.port}").build())
            val error = assertFailsWith<OAuthTokenException> { client.authorization().refreshToken("old-refresh") }
            assertEquals(OAuthTokenError.INVALID_GRANT, error.code)
            assertFalse(error.stackTraceToString().contains("do-not-log"))
        } finally { server.stop(0) }
    }

    @Test
    fun `deadline configuration rejects unbounded or negative durations`() {
        assertFailsWith<IllegalArgumentException> {
            StreamerSonglistConfiguration.builder().clientId("c").clientSecret("s").redirectUri("r")
                .requestTimeout(Duration.ZERO).build()
        }
    }
}
