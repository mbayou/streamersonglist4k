package com.mbayou.streamersonglist4k.songs

import com.fasterxml.jackson.annotation.JsonProperty
import com.mbayou.streamersonglist4k.api.ApiEnum
import com.mbayou.streamersonglist4k.api.AttributeId
import com.mbayou.streamersonglist4k.api.SongId
import com.mbayou.streamersonglist4k.api.StreamerId
import com.mbayou.streamersonglist4k.common.SongAttribute
import java.time.Instant

data class SongsResponseBody(
    @JsonProperty("\$schema")
    val schema: String? = null,
    val items: List<SongSummary>?,
    val total: Long,
    val token: String,
)

data class SongSummary(
    val id: SongId,
    val title: String,
    val artist: String,
    val createdAt: Instant,
    val active: Boolean,
    val lastActivation: Instant,
    val minAmount: Double,
    val comment: String,
    val capo: String,
    val bypassRequestLimits: Boolean,
    val streamerId: StreamerId,
    val timesPlayed: Int,
    val lastPlayed: Instant?,
    val lastPlayedFrom: String?,
    val numQueued: Int,
    val attributes: List<SongAttribute>?,
)

data class Song(
    @JsonProperty("\$schema")
    val schema: String? = null,
    val id: SongId,
    val title: String,
    val artist: String,
    val createdAt: Instant,
    val active: Boolean,
    val lastActivation: Instant,
    val comment: String,
    val tabs: String,
    val lyrics: String,
    val minAmount: Double,
    val chords: String,
    val capo: String,
    val learned: Boolean,
    val requestedBy: String,
    val bypassRequestLimits: Boolean,
    val streamerId: StreamerId,
    val timesPlayed: Int,
    val lastPlayed: Instant?,
    val lastPlayedFrom: String?,
)

data class SongDetail(
    @JsonProperty("\$schema")
    val schema: String? = null,
    val id: SongId,
    val title: String,
    val artist: String,
    val createdAt: Instant,
    val active: Boolean,
    val lastActivation: Instant,
    val minAmount: Double,
    val comment: String,
    val capo: String,
    val tabs: String,
    val lyrics: String,
    val chords: String,
    val bypassRequestLimits: Boolean,
    val streamerId: StreamerId,
    val timesPlayed: Int,
    val lastPlayed: Instant?,
    val lastPlayedFrom: String?,
    val numQueued: Int,
    val attributes: List<SongAttribute>?,
)

data class CreateSongRequest(
    val title: String,
    val artist: String,
    val streamerId: StreamerId,
    val attributeIds: List<AttributeId>? = null,
    val active: Boolean? = null,
    val bypassRequestLimits: Boolean? = null,
    val capo: String? = null,
    val chords: String? = null,
    val comment: String? = null,
    val lyrics: String? = null,
    val minAmount: Double? = null,
    val tabs: String? = null,
)

data class UpdateSongRequest(
    val title: String? = null,
    val artist: String? = null,
    val attributeIds: List<AttributeId>? = null,
    val active: Boolean? = null,
    val bypassRequestLimits: Boolean? = null,
    val capo: String? = null,
    val chords: String? = null,
    val comment: String? = null,
    val lyrics: String? = null,
    val minAmount: Double? = null,
    val tabs: String? = null,
)

data class DeleteSongResponseBody(
    @JsonProperty("\$schema")
    val schema: String? = null,
    val message: String,
)

data class ExportSongsResponseBody(
    @JsonProperty("\$schema")
    val schema: String? = null,
    val items: List<ExportSong>?,
    val total: Long,
)

data class ExportSong(
    val id: SongId? = null,
    val title: String,
    val artist: String,
)

data class ImportSongsRequest(
    val songs: List<ImportSong>,
)

data class ImportSong(
    val id: SongId? = null,
    val title: String,
    val artist: String,
)

data class ImportSongsResponseBody(
    @JsonProperty("\$schema")
    val schema: String? = null,
    val created: Int? = null,
    val updated: Int? = null,
)

data class BulkUpdateSongsRequest(
    val active: Boolean,
)

data class BulkUpdateSongsResponseBody(
    @JsonProperty("\$schema")
    val schema: String? = null,
    val updated: Int? = null,
)

enum class SongActiveFilter(override val apiValue: String) : ApiEnum {
    ACTIVE("true"),
    INACTIVE("false"),
}

enum class SongsOrderBy(override val apiValue: String) : ApiEnum {
    TITLE("title"),
    ARTIST("artist"),
    LAST_PLAYED("lastPlayed"),
    TIMES_PLAYED("timesPlayed"),
}

enum class TimesPlayedComparison(override val apiValue: String) : ApiEnum {
    EQ("eq"),
    LT("lt"),
    LTE("lte"),
    GT("gt"),
    GTE("gte"),
}

enum class InQueueFilter(override val apiValue: String) : ApiEnum {
    ALL("all"),
    EXCLUDE("exclude"),
    ONLY("only"),
}
