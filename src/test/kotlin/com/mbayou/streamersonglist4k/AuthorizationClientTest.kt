package com.mbayou.streamersonglist4k

import com.fasterxml.jackson.databind.ObjectMapper
import com.mbayou.streamersonglist4k.authorization.AuthorizationClient
import com.mbayou.streamersonglist4k.authorization.OAuthTokenResponse
import com.mbayou.streamersonglist4k.authorization.ScopePermission
import com.mbayou.streamersonglist4k.authorization.ScopeResource
import com.mbayou.streamersonglist4k.authorization.StreamerSonglistScope
import java.net.CookieHandler
import java.net.ProxySelector
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpHeaders
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.Optional
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLParameters
import javax.net.ssl.SSLSession
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AuthorizationClientTest {
    private val mapper = ObjectMapper().findAndRegisterModules()

    @Test
    fun `default oauth configuration targets the current staging issuer`() {
        val configuration = StreamerSonglistConfiguration.builder()
            .clientId("client-id")
            .clientSecret("client-secret")
            .redirectUri("https://app.example/callback")
            .build()
        val client = AuthorizationClient(HttpClient.newHttpClient(), mapper, configuration)

        val url = client.authorizationUrl(
            scopes = listOf(StreamerSonglistScope.resource(ScopeResource.STREAMER_QUEUE, ScopePermission.READ))
        )

        assertContains(url, "https://id.staging.streamersonglist.com/oauth2/auth?")
    }

    @Test
    fun `authorizationUrl builds an authorization-code url with encoded scopes and state`() {
        val configuration = StreamerSonglistConfiguration.builder()
            .clientId("client-id")
            .clientSecret("client-secret")
            .redirectUri("https://app.example/callback")
            .staging()
            .build()
        val client = AuthorizationClient(HttpClient.newHttpClient(), mapper, configuration)

        val url = client.authorizationUrl(
            scopes = listOf(
                StreamerSonglistScope.resource(ScopeResource.STREAMER_QUEUE, ScopePermission.READ),
                StreamerSonglistScope.wildcard(ScopeResource.STREAMER_SONG),
            ),
            state = "csrf state",
            nonce = "nonce value",
        )

        assertContains(url, "https://id.staging.streamersonglist.com/oauth2/auth?")
        assertContains(url, "client_id=client-id")
        assertContains(url, "redirect_uri=https%3A%2F%2Fapp.example%2Fcallback")
        assertContains(url, "scope=streamer.queue.r+streamer.song.*")
        assertContains(url, "state=csrf+state")
        assertContains(url, "nonce=nonce+value")
    }

    @Test
    fun `exchangeAuthorizationCode keeps the raw provider response for debugging`() {
        val rawBody = """
            {
              "access_token": "access-token-value-1234",
              "refresh_token": null,
              "expires_in": 3600,
              "scope": "streamer.song.* streamer.queue.*",
              "token_type": "Bearer"
            }
        """.trimIndent()
        val configuration = StreamerSonglistConfiguration.builder()
            .clientId("client-id")
            .clientSecret("client-secret")
            .redirectUri("https://app.example/callback")
            .staging()
            .build()
        val client = AuthorizationClient(
            FakeHttpClient(statusCode = 200, body = rawBody),
            mapper,
            configuration,
        )

        val token = client.exchangeAuthorizationCode("code-123")

        assertEquals(rawBody, token.rawResponseBody)
        assertNull(token.refreshToken)
        assertContains(token.maskedDebugResponseBody(), "\"access_token\": \"acce...1234 (len=23)\"")
    }

    @Test
    fun `maskSensitiveFields redacts token values in raw payloads`() {
        val masked = OAuthTokenResponse.maskSensitiveFields(
            """{"access_token":"access-token-value-1234","refresh_token":"refresh-token-value-9876","token_type":"Bearer"}"""
        )

        assertContains(masked, "\"access_token\":\"acce...1234 (len=23)\"")
        assertContains(masked, "\"refresh_token\":\"refr...9876 (len=24)\"")
        assertContains(masked, "\"token_type\":\"Bearer\"")
    }
}

private class FakeHttpClient(
    private val statusCode: Int,
    private val body: String,
) : HttpClient() {
    override fun cookieHandler(): Optional<CookieHandler> = Optional.empty()

    override fun connectTimeout(): Optional<Duration> = Optional.empty()

    override fun followRedirects(): Redirect = Redirect.NEVER

    override fun proxy(): Optional<ProxySelector> = Optional.empty()

    override fun sslContext(): SSLContext = SSLContext.getDefault()

    override fun sslParameters(): SSLParameters = SSLParameters()

    override fun authenticator(): Optional<java.net.Authenticator> = Optional.empty()

    override fun version(): Version = Version.HTTP_1_1

    override fun executor(): Optional<Executor> = Optional.empty()

    override fun <T : Any?> send(
        request: HttpRequest,
        responseBodyHandler: HttpResponse.BodyHandler<T>,
    ): HttpResponse<T> {
        @Suppress("UNCHECKED_CAST")
        return FakeHttpResponse(statusCode, body, request) as HttpResponse<T>
    }

    override fun <T : Any?> sendAsync(
        request: HttpRequest,
        responseBodyHandler: HttpResponse.BodyHandler<T>,
    ): CompletableFuture<HttpResponse<T>> {
        return CompletableFuture.completedFuture(send(request, responseBodyHandler))
    }

    override fun <T : Any?> sendAsync(
        request: HttpRequest,
        responseBodyHandler: HttpResponse.BodyHandler<T>,
        pushPromiseHandler: HttpResponse.PushPromiseHandler<T>?,
    ): CompletableFuture<HttpResponse<T>> {
        return CompletableFuture.completedFuture(send(request, responseBodyHandler))
    }
}

private class FakeHttpResponse(
    private val statusCode: Int,
    private val body: String,
    private val request: HttpRequest,
) : HttpResponse<String> {
    override fun statusCode(): Int = statusCode

    override fun request(): HttpRequest = request

    override fun previousResponse(): Optional<HttpResponse<String>> = Optional.empty()

    override fun headers(): HttpHeaders = HttpHeaders.of(emptyMap()) { _, _ -> true }

    override fun body(): String = body

    override fun sslSession(): Optional<SSLSession> = Optional.empty()

    override fun uri(): URI = request.uri()

    override fun version(): HttpClient.Version = HttpClient.Version.HTTP_1_1
}
