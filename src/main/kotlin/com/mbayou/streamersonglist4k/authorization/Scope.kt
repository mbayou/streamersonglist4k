package com.mbayou.streamersonglist4k.authorization

import com.mbayou.streamersonglist4k.api.ApiEnum

data class StreamerSonglistScope(val value: String) {
    override fun toString(): String = value

    companion object {
        val OFFLINE_ACCESS = StreamerSonglistScope("offline_access")
        val OPENID = StreamerSonglistScope("openid")

        fun resource(resource: ScopeResource, permission: ScopePermission): StreamerSonglistScope =
            StreamerSonglistScope("${resource.value}.${permission.apiValue}")

        fun wildcard(resource: ScopeResource): StreamerSonglistScope =
            StreamerSonglistScope("${resource.value}.*")

        fun raw(value: String): StreamerSonglistScope = StreamerSonglistScope(value)
    }
}

enum class ScopeResource(val value: String) {
    STREAMER_ACTION_LOG("streamer.action-log"),
    STREAMER_ATTRIBUTE("streamer.attribute"),
    STREAMER_COMMAND("streamer.command"),
    STREAMER_LEARN_LIST("streamer.learn-list"),
    STREAMER_OVERLAY("streamer.overlay"),
    STREAMER_PERMIT("streamer.permit"),
    STREAMER_PLAY_HISTORY("streamer.play-history"),
    STREAMER_QUEUE("streamer.queue"),
    STREAMER_SETTINGS("streamer.settings"),
    STREAMER_SONG("streamer.song"),
    STREAMER_TOKEN("streamer.token"),
    USER("user"),
    USER_FAVORITE("user.favorite"),
    USER_PREFERENCE("user.preference"),
    USER_SONG_REQUEST("user.song-request"),
}

enum class ScopePermission(override val apiValue: String) : ApiEnum {
    READ("read"),
    WRITE("write"),
}

enum class PkceCodeChallengeMethod(override val apiValue: String) : ApiEnum {
    S256("S256"),
    PLAIN("plain"),
}

data class OAuthPkceChallenge(
    val value: String,
    val method: PkceCodeChallengeMethod = PkceCodeChallengeMethod.S256,
) {
    init {
        require(value.isNotBlank()) { "PKCE code challenge must not be blank" }
    }
}
