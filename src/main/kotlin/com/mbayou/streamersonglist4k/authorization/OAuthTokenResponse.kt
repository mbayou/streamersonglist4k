package com.mbayou.streamersonglist4k.authorization

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

data class OAuthTokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("refresh_token")
    val refreshToken: String? = null,
    @JsonProperty("expires_in")
    val expiresIn: Long,
    val scope: String,
    @JsonProperty("token_type")
    val tokenType: String,
    @JsonIgnore
    val rawResponseBody: String? = null,
) {
    fun maskedDebugResponseBody(): String {
        return rawResponseBody?.let(::maskSensitiveFields) ?: buildString {
            append("{")
            append("\"access_token\":\"").append(maskTokenValue(accessToken)).append("\",")
            append("\"refresh_token\":")
            if (refreshToken == null) {
                append("null")
            } else {
                append("\"").append(maskTokenValue(refreshToken)).append("\"")
            }
            append(",\"expires_in\":").append(expiresIn)
            append(",\"scope\":\"").append(scope).append("\"")
            append(",\"token_type\":\"").append(tokenType).append("\"")
            append("}")
        }
    }

    companion object {
        private val sensitiveFieldPattern = Regex(
            """("(?:access_token|refresh_token|id_token|client_secret)"\s*:\s*")([^"]*)(")""",
            setOf(RegexOption.IGNORE_CASE)
        )

        fun maskSensitiveFields(body: String): String {
            return sensitiveFieldPattern.replace(body) { match ->
                "${match.groupValues[1]}${maskTokenValue(match.groupValues[2])}${match.groupValues[3]}"
            }
        }

        private fun maskTokenValue(value: String): String {
            if (value.isEmpty()) {
                return "[redacted len=0]"
            }
            if (value.length <= 8) {
                return "[redacted len=${value.length}]"
            }
            return "${value.take(4)}...${value.takeLast(4)} (len=${value.length})"
        }
    }
}
