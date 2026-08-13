package com.mbayou.streamersonglist4k

data class StreamerSonglistConfiguration(
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
    val apiBaseUrl: String,
    val oAuthBaseUrl: String,
    val eventsBaseUrl: String,
) {
    companion object {
        @JvmStatic
        fun builder(): Builder = Builder()
    }

    class Builder {
        private var clientId: String? = null
        private var clientSecret: String? = null
        private var redirectUri: String? = null
        private var apiBaseUrl: String = "https://api.stsl.io"
        private var oAuthBaseUrl: String = "https://id.stsl.io/oauth2"
        private var eventsBaseUrl: String = "wss://events.streamersonglist.com/connection/websocket"

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
            )
        }
    }
}
