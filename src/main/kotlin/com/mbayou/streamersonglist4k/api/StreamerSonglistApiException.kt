package com.mbayou.streamersonglist4k.api

class StreamerSonglistApiException(
    val statusCode: Int,
    val error: ErrorModel?,
    val responseBody: String,
) : RuntimeException(buildMessage(statusCode, error, responseBody)) {
    companion object {
        private fun buildMessage(statusCode: Int, error: ErrorModel?, responseBody: String): String {
            val title = error?.title
            val detail = error?.detail
            return when {
                !title.isNullOrBlank() && !detail.isNullOrBlank() -> "StreamerSonglist API error $statusCode: $title - $detail"
                !title.isNullOrBlank() -> "StreamerSonglist API error $statusCode: $title"
                responseBody.isNotBlank() -> "StreamerSonglist API error $statusCode: $responseBody"
                else -> "StreamerSonglist API error $statusCode"
            }
        }
    }
}
