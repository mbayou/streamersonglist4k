package com.mbayou.streamersonglist4k.api

import com.fasterxml.jackson.annotation.JsonProperty

data class ErrorModel(
    @JsonProperty("\$schema")
    val schema: String? = null,
    val type: String? = null,
    val title: String? = null,
    val status: Long? = null,
    val detail: String? = null,
    val instance: String? = null,
    val errors: List<ErrorDetail>? = null,
)

data class ErrorDetail(
    val location: String? = null,
    val message: String? = null,
    val value: Any? = null,
)
