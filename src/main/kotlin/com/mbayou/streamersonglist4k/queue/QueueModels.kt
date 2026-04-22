package com.mbayou.streamersonglist4k.queue

import com.fasterxml.jackson.annotation.JsonProperty
import com.mbayou.streamersonglist4k.api.ApiEnum
import com.mbayou.streamersonglist4k.api.QueueId
import com.mbayou.streamersonglist4k.api.SongId
import com.mbayou.streamersonglist4k.api.StreamerId
import com.mbayou.streamersonglist4k.common.QueueRequestInput
import com.mbayou.streamersonglist4k.common.QueueSongAttribute
import com.mbayou.streamersonglist4k.common.Request
import java.time.Instant

data class QueueResponseBody(
    @JsonProperty("\$schema")
    val schema: String? = null,
    val items: List<QueueDetails>?,
    val total: Long,
)

data class QueueDetails(
    @JsonProperty("\$schema")
    val schema: String? = null,
    val id: QueueId,
    val createdAt: Instant,
    val songId: SongId?,
    val nonlistSong: String?,
    val note: String?,
    val streamerId: StreamerId,
    val position: Long,
    val song: QueueSong,
    val requests: List<Request>?,
)

data class QueueSong(
    val title: String,
    val artist: String,
    val lastPlayed: Instant?,
    val lastPlayedFrom: String? = null,
    val timesPlayed: Int,
    val comment: String?,
    val capo: String?,
    val attributes: List<QueueSongAttribute>?,
)

data class CreateQueueRequest(
    val streamerId: StreamerId,
    val songId: SongId? = null,
    val query: String? = null,
    val nonlistSong: String? = null,
    val requests: List<QueueRequestInput>? = null,
    val note: String? = null,
    val insertMethod: QueueInsertMethod? = null,
    val position: Int? = null,
)

data class UpdateQueueRequest(
    val songId: SongId? = null,
    val nonlistSong: String? = null,
    val position: Int? = null,
)

data class AddQueueRequestRequest(
    val name: String,
    val amount: Double,
    val requestText: String? = null,
    val source: String? = null,
)

data class UpdateQueueRequestRequest(
    val name: String? = null,
    val amount: Double? = null,
    val requestText: String? = null,
)

data class ClearQueueResponseBody(
    @JsonProperty("\$schema")
    val schema: String? = null,
    val message: String,
)

data class DeleteQueueRequestResponseBody(
    @JsonProperty("\$schema")
    val schema: String? = null,
    val message: String,
)

enum class QueueInsertMethod(override val apiValue: String) : ApiEnum {
    POSITION("position"),
    AMOUNT("amount"),
    END("end"),
}
