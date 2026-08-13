package com.mbayou.streamersonglist4k.api

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.mbayou.streamersonglist4k.StreamerSonglistConfiguration
import java.io.IOException
import java.lang.reflect.Array
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.util.StringJoiner

abstract class ApiClient(
    protected val httpClient: HttpClient,
    protected val mapper: ObjectMapper,
    protected val configuration: StreamerSonglistConfiguration,
    protected val defaultAuthentication: StreamerSonglistAuthentication?,
) {
    protected fun get(path: String): RequestBuilder = RequestBuilder("GET", path)
    protected fun post(path: String): RequestBuilder = RequestBuilder("POST", path)
    protected fun patch(path: String): RequestBuilder = RequestBuilder("PATCH", path)
    protected fun put(path: String): RequestBuilder = RequestBuilder("PUT", path)
    protected fun delete(path: String): RequestBuilder = RequestBuilder("DELETE", path)

    protected fun requiredAuthentication(authentication: StreamerSonglistAuthentication?): StreamerSonglistAuthentication =
        authentication ?: defaultAuthentication ?: error("Authentication is required for this request")

    inner class RequestBuilder(
        private val method: String,
        private var path: String,
    ) {
        private val queryParams = linkedMapOf<String, Any?>()
        private var bodyObject: Any? = null
        private var authentication: StreamerSonglistAuthentication? = null

        fun queryParams(params: Map<String, Any?>?): RequestBuilder = apply {
            params?.forEach { (key, value) -> queryParams[key] = value }
        }

        fun pathParam(name: String, value: Any): RequestBuilder = apply {
            val placeholder = "\\{$name\\}".toRegex()
            path = path.replace(placeholder, encode(apiValue(value)))
        }

        fun body(bodyObject: Any?): RequestBuilder = apply {
            this.bodyObject = bodyObject
        }

        fun authentication(authentication: StreamerSonglistAuthentication?): RequestBuilder = apply {
            this.authentication = authentication
        }

        fun <T> send(typeRef: TypeReference<T>): T {
            val response = sendRequest()
            val responseBody = response.body()
            if (response.statusCode() !in 200..299) {
                throw buildException(response.statusCode(), responseBody)
            }
            return mapper.readValue(responseBody, typeRef)
        }

        fun sendNoContent() {
            val response = sendRequest()
            val responseBody = response.body()
            if (response.statusCode() !in 200..299) {
                throw buildException(response.statusCode(), responseBody)
            }
        }

        private fun sendRequest(): HttpResponse<String> {
            val requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(buildUrl(configuration.apiBaseUrl + path, queryParams)))
                .header("Accept", "application/json")

            val auth = authentication
            if (auth != null) {
                requestBuilder.header("Authorization", auth.headerValue)
                if (auth is StreamerSonglistAuthentication.OAuthAccessToken) {
                    requestBuilder.header("Client-Id", configuration.clientId)
                }
            }

            if ("GET".equals(method, ignoreCase = true)) {
                requestBuilder.GET()
            } else {
                val jsonBody = bodyObject?.let { mapper.writeValueAsString(it) } ?: ""
                requestBuilder.header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(jsonBody))
            }

            return try {
                httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
            } catch (exception: InterruptedException) {
                Thread.currentThread().interrupt()
                throw RuntimeException("Failed to send API request", exception)
            } catch (exception: IOException) {
                throw RuntimeException("Failed to send API request", exception)
            }
        }

        private fun buildException(statusCode: Int, responseBody: String): StreamerSonglistApiException {
            val error = runCatching {
                mapper.readValue(responseBody, ErrorModel::class.java)
            }.getOrNull()
            return StreamerSonglistApiException(statusCode, error, responseBody)
        }
    }

    protected inline fun <reified T> responseType(): TypeReference<T> = object : TypeReference<T>() {}

    companion object {
        fun buildUrl(path: String, queryParams: Map<String, Any?>): String {
            val sanitized = queryParams.filterValues { it != null }
            if (sanitized.isEmpty()) {
                return path
            }

            val joiner = StringJoiner("&", "$path?", "")
            sanitized.forEach { (key, value) ->
                appendQuery(joiner, key, value)
            }
            return joiner.toString()
        }

        fun encode(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)

        fun apiValue(value: Any): String = when (value) {
            is IntIdentifier -> value.value.toString()
            is ApiEnum -> value.apiValue
            else -> value.toString()
        }

        private fun appendQuery(joiner: StringJoiner, key: String, value: Any?) {
            when {
                value == null -> Unit
                value is Iterable<*> -> value.forEach { item -> appendQuery(joiner, key, item) }
                value.javaClass.isArray -> {
                    val length = Array.getLength(value)
                    for (index in 0 until length) {
                        appendQuery(joiner, key, Array.get(value, index))
                    }
                }
                else -> joiner.add("${encode(key)}=${encode(apiValue(value))}")
            }
        }
    }
}
