package com.mbayou.streamersonglist4k.authorization

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
)
