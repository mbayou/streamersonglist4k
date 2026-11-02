package com.mbayou.streamersonglist4k.api

open class StreamerSonglistApiException(
    val statusCode: Int,
    val error: ErrorModel?,
    val responseBody: String,
) : RuntimeException("StreamerSonglist API error $statusCode")
