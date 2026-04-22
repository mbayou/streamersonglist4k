package com.mbayou.streamersonglist4k.authorization

import com.mbayou.streamersonglist4k.api.ApiEnum

data class StreamerSonglistScope(val value: String) {
    override fun toString(): String = value

    companion object {
        val STREAMER_READ = StreamerSonglistScope("streamer.*.r")
        val USER_READ = StreamerSonglistScope("user.*.r")

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
    STREAMER_INTEGRATION("streamer.integration"),
    STREAMER_INTEGRATIONS("streamer.integrations"),
    STREAMER_OVERLAY("streamer.overlay"),
    STREAMER_PLAY_HISTORY("streamer.play-history"),
    STREAMER_QUEUE("streamer.queue"),
    STREAMER_SETTING("streamer.setting"),
    STREAMER_SETTINGS("streamer.settings"),
    STREAMER_SONG("streamer.song"),
    USER_PREFERENCE("user.preference"),
    USER_SONG_REQUEST("user.song-request"),
}

enum class ScopePermission(override val apiValue: String) : ApiEnum {
    CREATE("c"),
    READ("r"),
    UPDATE("u"),
    DELETE("d"),
    SEARCH("s"),
}
