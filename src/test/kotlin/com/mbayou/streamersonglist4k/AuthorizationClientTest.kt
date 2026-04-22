package com.mbayou.streamersonglist4k

import com.fasterxml.jackson.databind.ObjectMapper
import com.mbayou.streamersonglist4k.authorization.AuthorizationClient
import com.mbayou.streamersonglist4k.authorization.ScopePermission
import com.mbayou.streamersonglist4k.authorization.ScopeResource
import com.mbayou.streamersonglist4k.authorization.StreamerSonglistScope
import java.net.http.HttpClient
import kotlin.test.Test
import kotlin.test.assertContains

class AuthorizationClientTest {
    @Test
    fun `authorizationUrl builds an authorization-code url with encoded scopes and state`() {
        val configuration = StreamerSonglistConfiguration.builder()
            .clientId("client-id")
            .clientSecret("client-secret")
            .redirectUri("https://app.example/callback")
            .staging()
            .build()
        val client = AuthorizationClient(HttpClient.newHttpClient(), ObjectMapper(), configuration)

        val url = client.authorizationUrl(
            scopes = listOf(
                StreamerSonglistScope.resource(ScopeResource.STREAMER_QUEUE, ScopePermission.READ),
                StreamerSonglistScope.wildcard(ScopeResource.STREAMER_SONG),
            ),
            state = "csrf state",
            nonce = "nonce value",
        )

        assertContains(url, "https://id.staging.streamersonglist.com/oauth2/authorize?")
        assertContains(url, "client_id=client-id")
        assertContains(url, "redirect_uri=https%3A%2F%2Fapp.example%2Fcallback")
        assertContains(url, "scope=streamer.queue.r+streamer.song.*")
        assertContains(url, "state=csrf+state")
        assertContains(url, "nonce=nonce+value")
    }
}
