package com.mbayou.streamersonglist4k.api

sealed interface StreamerLookup {
    fun queryParams(): Map<String, Any?>

    data class ById(val streamerId: StreamerId) : StreamerLookup {
        override fun queryParams(): Map<String, Any?> = mapOf("streamer_id" to streamerId)
    }

    data class ByName(val streamerName: String, val platform: StreamerPlatform) : StreamerLookup {
        override fun queryParams(): Map<String, Any?> = mapOf(
            "streamer_name" to streamerName,
            "platform" to platform,
        )
    }

    data class ByPlatformId(val platformId: String, val platform: StreamerPlatform) : StreamerLookup {
        override fun queryParams(): Map<String, Any?> = mapOf(
            "platform_id" to platformId,
            "platform" to platform,
        )
    }
}

data class CursorPage(
    val limit: Int? = null,
    val after: String? = null,
) {
    internal fun queryParams(): Map<String, Any?> = mapOf(
        "limit" to limit,
        "after" to after,
    )
}
