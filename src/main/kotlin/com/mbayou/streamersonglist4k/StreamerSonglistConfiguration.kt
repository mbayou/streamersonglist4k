package com.mbayou.streamersonglist4k

import java.time.Duration

data class StreamerSonglistConfiguration(
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
    val apiBaseUrl: String,
    val oAuthBaseUrl: String,
    val eventsBaseUrl: String,
    val requestTimeout: Duration = Duration.ofSeconds(20),
    val eventCommandTimeout: Duration = Duration.ofSeconds(15),
) {
    init {
        require(!requestTimeout.isNegative && !requestTimeout.isZero)
        require(!eventCommandTimeout.isNegative && !eventCommandTimeout.isZero)
    }

    companion object {
        @JvmStatic
        fun builder(): Builder = Builder()
    }

    class Builder {
        private var clientId: String? = null
        private var clientSecret: String? = null
        private var redirectUri: String? = null
        private var apiBaseUrl: String = "https://api.streamersonglist.com"
        private var oAuthBaseUrl: String = "https://id.streamersonglist.com/oauth2"
        private var eventsBaseUrl: String = "wss://events.streamersonglist.com/connection/websocket"

        private var requestTimeout = Duration.ofSeconds(20)
        private var eventCommandTimeout = Duration.ofSeconds(15)

        fun requestTimeout(timeout: Duration) = apply { requestTimeout = timeout }
        fun eventCommandTimeout(timeout: Duration) = apply { eventCommandTimeout = timeout }

        fun clientId(clientId: String) = apply { this.clientId = clientId }
        fun clientSecret(clientSecret: String) = apply { this.clientSecret = clientSecret }
        fun redirectUri(redirectUri: String) = apply { this.redirectUri = redirectUri }
        fun apiBaseUrl(apiBaseUrl: String) = apply { this.apiBaseUrl = apiBaseUrl.trimEnd('/') }
        fun oAuthBaseUrl(oAuthBaseUrl: String) = apply { this.oAuthBaseUrl = oAuthBaseUrl.trimEnd('/') }
        fun eventsBaseUrl(eventsBaseUrl: String) = apply { this.eventsBaseUrl = eventsBaseUrl }

        fun staging() = apply {
            apiBaseUrl = "https://api.staging.streamersonglist.com"
            oAuthBaseUrl = "https://id.staging.streamersonglist.com/oauth2"
            eventsBaseUrl = "wss://events.staging.streamersonglist.com/connection/websocket"
        }

        fun build(): StreamerSonglistConfiguration {
            return StreamerSonglistConfiguration(
                clientId = requireNotNull(clientId) { "ClientId is required" },
                clientSecret = requireNotNull(clientSecret) { "ClientSecret is required" },
                redirectUri = requireNotNull(redirectUri) { "RedirectUri is required" },
                apiBaseUrl = apiBaseUrl.trimEnd('/'),
                oAuthBaseUrl = oAuthBaseUrl.trimEnd('/'),
                eventsBaseUrl = eventsBaseUrl,
                requestTimeout = requestTimeout,
                eventCommandTimeout = eventCommandTimeout,
            )
        }
    }
}
