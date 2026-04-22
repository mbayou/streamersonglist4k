package com.mbayou.streamersonglist4k.common

import com.fasterxml.jackson.annotation.JsonProperty
import com.mbayou.streamersonglist4k.api.ApiEnum
import com.mbayou.streamersonglist4k.api.RequestId

data class Request(
    val id: RequestId,
    val name: String,
    val amount: Double,
    val requestText: String,
    val source: String,
    val createdAt: String,
    val user: RequestUser? = null,
)

data class RequestUser(
    val username: String,
    val platform: String,
)

data class QueueSongAttribute(
    val name: String,
    val image: String? = null,
)

data class SongAttribute(
    val id: com.mbayou.streamersonglist4k.api.AttributeId,
    val name: String,
)

enum class RequestUserType(override val apiValue: String) : ApiEnum {
    CUSTOM("custom"),
    TWITCH("twitch"),
    YOUTUBE("youtube"),
}

data class QueueRequestInput(
    val name: String?,
    val amount: Double,
    val note: String? = null,
    val userId: Int? = null,
    val userType: RequestUserType = RequestUserType.CUSTOM,
)

data class SavedQueueRequestInput(
    val name: String,
    val amount: Double,
    val note: String? = null,
    val source: String,
    val userId: Int? = null,
)

data class PlayHistoryRequestInput(
    @JsonProperty("Name")
    val name: String,
    @JsonProperty("Amount")
    val amount: Double,
    @JsonProperty("Note")
    val note: String? = null,
    @JsonProperty("RequestText")
    val requestText: String? = null,
    @JsonProperty("Source")
    val source: String,
    @JsonProperty("UserID")
    val userId: Int? = null,
    @JsonProperty("TransactionID")
    val transactionId: Int? = null,
)
