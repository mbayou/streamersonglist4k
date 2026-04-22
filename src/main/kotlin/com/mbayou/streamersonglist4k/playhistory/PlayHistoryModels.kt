package com.mbayou.streamersonglist4k.playhistory

import com.fasterxml.jackson.annotation.JsonProperty
import com.mbayou.streamersonglist4k.api.ApiEnum
import com.mbayou.streamersonglist4k.api.PlayHistoryId
import com.mbayou.streamersonglist4k.api.SongId
import com.mbayou.streamersonglist4k.api.StreamerId
import com.mbayou.streamersonglist4k.common.PlayHistoryRequestInput
import com.mbayou.streamersonglist4k.common.Request
import com.mbayou.streamersonglist4k.queue.QueueSong
import java.time.Instant

data class PlayHistoryResponseBody(
    @JsonProperty("\$schema")
    val schema: String? = null,
    val items: List<PlayHistoryDetails>?,
    val total: Long,
    val token: String,
)

data class PlayHistoryDetails(
    val id: PlayHistoryId,
    val createdAt: Instant,
    val donationAmount: Double,
    val nonlistSong: String?,
    val songId: SongId?,
    val streamerId: StreamerId,
    val playedAt: Instant,
    val note: String?,
    val song: PlayHistorySong,
    val requests: List<Request>?,
)

data class PlayHistorySong(
    val title: String,
    val artist: String,
)

data class HistoryDetails(
    @JsonProperty("\$schema")
    val schema: String? = null,
    val id: PlayHistoryId,
    val createdAt: Instant,
    val nonlistSong: String?,
    val songId: SongId?,
    val streamerId: StreamerId,
    val playedAt: Instant,
    val note: String?,
    val song: QueueSong,
    val requests: List<Request>?,
)

data class CreatePlayHistoryRequest(
    val streamerId: StreamerId,
    val songId: SongId? = null,
    val nonlistSong: String? = null,
    val note: String? = null,
    val playedAt: Instant? = null,
    val requests: List<PlayHistoryRequestInput>? = null,
)

data class UpdatePlayHistoryRequest(
    val note: String?,
    val nonlistSong: String?,
    val playedAt: Instant? = null,
)

data class ExportPlayHistoryResponseBody(
    @JsonProperty("\$schema")
    val schema: String? = null,
    val items: List<PlayHistoryDetails>?,
    val total: Long,
)

enum class PlayHistoryOrderBy(override val apiValue: String) : ApiEnum {
    PLAYED_AT("played_at"),
    SONG("song"),
    REQUESTER("requester"),
}
