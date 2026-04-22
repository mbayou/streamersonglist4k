package com.mbayou.streamersonglist4k.users

import com.fasterxml.jackson.annotation.JsonProperty
import com.mbayou.streamersonglist4k.api.UserId

data class UserResponseBody(
    @JsonProperty("\$schema")
    val schema: String? = null,
    val id: UserId,
)
