package com.mbayou.streamersonglist4k.authorization

import com.fasterxml.jackson.databind.ObjectMapper
import com.mbayou.streamersonglist4k.StreamerSonglistConfiguration
import com.mbayou.streamersonglist4k.api.ErrorModel
import com.mbayou.streamersonglist4k.api.StreamerSonglistApiException
import java.io.IOException
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.util.StringJoiner

class AuthorizationClient(
    private val httpClient: HttpClient,
    private val mapper: ObjectMapper,
    private val configuration: StreamerSonglistConfiguration,
) {
    fun authorizationUrl(
        scopes: Collection<StreamerSonglistScope>,
        state: String? = null,
        nonce: String? = null,
    ): String {
        val params = linkedMapOf<String, String?>(
            "client_id" to configuration.clientId,
            "redirect_uri" to configuration.redirectUri,
            "response_type" to "code",
            "scope" to scopes.joinToString(" ") { it.value },
            "state" to state,
            "nonce" to nonce,
        )
        val query = StringJoiner("&")
        params.filterValues { it != null }.forEach { (key, value) ->
            query.add("${encode(key)}=${encode(value!!)}")
        }
        return "${configuration.oAuthBaseUrl}/auth?$query"
    }

    fun exchangeAuthorizationCode(code: String): OAuthTokenResponse {
        return token(
            mapOf(
                "client_id" to configuration.clientId,
                "client_secret" to configuration.clientSecret,
                "redirect_uri" to configuration.redirectUri,
                "grant_type" to "authorization_code",
                "code" to code,
            )
        )
    }

    fun clientCredentials(scopes: Collection<StreamerSonglistScope>): OAuthTokenResponse {
        return token(
            mapOf(
                "client_id" to configuration.clientId,
                "client_secret" to configuration.clientSecret,
                "grant_type" to "client_credentials",
                "scope" to scopes.joinToString(" ") { it.value },
            )
        )
    }

    fun refreshToken(refreshToken: String): OAuthTokenResponse {
        return token(
            mapOf(
                "client_id" to configuration.clientId,
                "client_secret" to configuration.clientSecret,
                "redirect_uri" to configuration.redirectUri,
                "grant_type" to "refresh_token",
                "refresh_token" to refreshToken,
            )
        )
    }

    private fun token(params: Map<String, String>): OAuthTokenResponse {
        val formBody = params.entries.joinToString("&") { (key, value) ->
            "${encode(key)}=${encode(value)}"
        }
        val request = HttpRequest.newBuilder()
            .uri(URI.create("${configuration.oAuthBaseUrl}/token"))
            .header("Accept", "application/json")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(formBody))
            .build()

        val response = send(request)
        val body = response.body()
        if (response.statusCode() !in 200..299) {
            val error = runCatching { mapper.readValue(body, ErrorModel::class.java) }.getOrNull()
            throw StreamerSonglistApiException(response.statusCode(), error, body)
        }
        val tokenResponse = mapper.readValue(body, OAuthTokenResponse::class.java)
        return tokenResponse.copy(rawResponseBody = body)
    }

    private fun send(request: HttpRequest): HttpResponse<String> {
        return try {
            httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        } catch (exception: InterruptedException) {
            Thread.currentThread().interrupt()
            throw RuntimeException("Failed to send OAuth request", exception)
        } catch (exception: IOException) {
            throw RuntimeException("Failed to send OAuth request", exception)
        }
    }

    private fun encode(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)
}
