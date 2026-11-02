package com.mbayou.streamersonglist4k.authorization

import com.mbayou.streamersonglist4k.api.ErrorModel
import com.mbayou.streamersonglist4k.api.StreamerSonglistApiException

/** OAuth token errors are not REST authorization errors. Never log the raw body. */
class OAuthTokenException(
    statusCode: Int,
    errorModel: ErrorModel?,
    responseBody: String,
    errorCode: String?,
) : StreamerSonglistApiException(statusCode, errorModel, responseBody) {
    val code: OAuthTokenError = OAuthTokenError.entries.firstOrNull { it.value == errorCode } ?: OAuthTokenError.UNKNOWN
}

enum class OAuthTokenError(val value: String) {
    INVALID_GRANT("invalid_grant"), INVALID_CLIENT("invalid_client"),
    INVALID_REQUEST("invalid_request"), INVALID_SCOPE("invalid_scope"),
    UNAUTHORIZED_CLIENT("unauthorized_client"), UNSUPPORTED_GRANT_TYPE("unsupported_grant_type"),
    SERVER_ERROR("server_error"), TEMPORARILY_UNAVAILABLE("temporarily_unavailable"), UNKNOWN("unknown"),
}
