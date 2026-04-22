package com.mbayou.streamersonglist4k.songs

import com.fasterxml.jackson.databind.ObjectMapper
import com.mbayou.streamersonglist4k.StreamerSonglistConfiguration
import com.mbayou.streamersonglist4k.api.ApiClient
import com.mbayou.streamersonglist4k.api.AttributeId
import com.mbayou.streamersonglist4k.api.CursorPage
import com.mbayou.streamersonglist4k.api.SongId
import com.mbayou.streamersonglist4k.api.SortDirection
import com.mbayou.streamersonglist4k.api.StreamerLookup
import com.mbayou.streamersonglist4k.api.StreamerSonglistAuthentication
import java.net.http.HttpClient
import java.time.Instant

class SongsClient(
    httpClient: HttpClient,
    mapper: ObjectMapper,
    configuration: StreamerSonglistConfiguration,
    defaultAuthentication: StreamerSonglistAuthentication?,
) : ApiClient(httpClient, mapper, configuration, defaultAuthentication) {
    fun getSongs(
        request: GetSongsRequest,
        authentication: StreamerSonglistAuthentication? = null,
    ): SongsResponseBody {
        return get("/songs")
            .queryParams(request.queryParams())
            .authentication(authentication ?: defaultAuthentication)
            .send(responseType())
    }

    fun getAllSongs(
        request: GetSongsRequest,
        authentication: StreamerSonglistAuthentication? = null,
    ): SongsResponseBody {
        return get("/songs/all")
            .queryParams(request.queryParams())
            .authentication(requiredAuthentication(authentication))
            .send(responseType())
    }

    fun getSong(
        songId: SongId,
        authentication: StreamerSonglistAuthentication? = null,
    ): SongDetail {
        return get("/songs/{song_id}")
            .pathParam("song_id", songId)
            .authentication(authentication ?: defaultAuthentication)
            .send(responseType())
    }

    fun createSong(
        request: CreateSongRequest,
        authentication: StreamerSonglistAuthentication? = null,
    ): Song {
        return post("/songs")
            .body(request)
            .authentication(requiredAuthentication(authentication))
            .send(responseType())
    }

    fun updateSong(
        songId: SongId,
        request: UpdateSongRequest,
        authentication: StreamerSonglistAuthentication? = null,
    ): SongDetail {
        return patch("/songs/{song_id}")
            .pathParam("song_id", songId)
            .body(request)
            .authentication(requiredAuthentication(authentication))
            .send(responseType())
    }

    fun deleteSong(
        songId: SongId,
        authentication: StreamerSonglistAuthentication? = null,
    ): DeleteSongResponseBody {
        return delete("/songs/{song_id}")
            .pathParam("song_id", songId)
            .authentication(requiredAuthentication(authentication))
            .send(responseType())
    }

    fun exportSongs(
        lookup: StreamerLookup,
        authentication: StreamerSonglistAuthentication? = null,
    ): ExportSongsResponseBody {
        return get("/songs/export")
            .queryParams(lookup.queryParams())
            .authentication(requiredAuthentication(authentication))
            .send(responseType())
    }

    fun bulkUpdateSongs(
        lookup: StreamerLookup,
        request: BulkUpdateSongsRequest,
        authentication: StreamerSonglistAuthentication? = null,
    ): BulkUpdateSongsResponseBody {
        return post("/songs/bulk-update")
            .queryParams(lookup.queryParams())
            .body(request)
            .authentication(requiredAuthentication(authentication))
            .send(responseType())
    }
}

data class GetSongsRequest(
    val lookup: StreamerLookup,
    val active: SongActiveFilter? = null,
    val attributes: List<AttributeId>? = null,
    val search: String? = null,
    val isNew: Boolean? = null,
    val orderBy: SongsOrderBy? = null,
    val orderDirection: SortDirection? = null,
    val lastPlayedBefore: Instant? = null,
    val lastPlayedAfter: Instant? = null,
    val timesPlayedComparison: TimesPlayedComparison? = null,
    val timesPlayedValue: Int? = null,
    val inQueue: InQueueFilter? = null,
    val page: CursorPage = CursorPage(),
) {
    internal fun queryParams(): Map<String, Any?> = lookup.queryParams() + page.queryParams() + mapOf(
        "active" to active,
        "attributes" to attributes,
        "search" to search,
        "is_new" to isNew,
        "order_by" to orderBy,
        "order_dir" to orderDirection,
        "last_played_before" to lastPlayedBefore,
        "last_played_after" to lastPlayedAfter,
        "times_played_op" to timesPlayedComparison,
        "times_played_value" to timesPlayedValue,
        "in_queue" to inQueue,
    )
}
