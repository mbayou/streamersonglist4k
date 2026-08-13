package com.mbayou.streamersonglist4k.api

sealed interface StreamerSonglistAuthentication {
    val value: String
    val headerValue: String

    data class OAuthAccessToken(override val value: String) : StreamerSonglistAuthentication {
        override val headerValue: String = "Bearer $value"
    }

    data class StreamerAccessToken(override val value: String) : StreamerSonglistAuthentication {
        override val headerValue: String = "Streamer $value"
    }

    data class UserAccessToken(override val value: String) : StreamerSonglistAuthentication {
        override val headerValue: String = "User $value"
    }
}
