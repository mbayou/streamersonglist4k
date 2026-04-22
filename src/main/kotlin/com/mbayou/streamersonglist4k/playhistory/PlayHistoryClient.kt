package com.mbayou.streamersonglist4k.playhistory

import com.fasterxml.jackson.databind.ObjectMapper
import com.mbayou.streamersonglist4k.StreamerSonglistConfiguration
import com.mbayou.streamersonglist4k.api.ApiClient
import com.mbayou.streamersonglist4k.api.CursorPage
import com.mbayou.streamersonglist4k.api.PlayHistoryId
import com.mbayou.streamersonglist4k.api.SongId
import com.mbayou.streamersonglist4k.api.SortDirection
import com.mbayou.streamersonglist4k.api.StreamerLookup
import com.mbayou.streamersonglist4k.api.StreamerSonglistAuthentication
import java.net.http.HttpClient
import java.time.Instant

class PlayHistoryClient(
    httpClient: HttpClient,
    mapper: ObjectMapper,
    configuration: StreamerSonglistConfiguration,
    defaultAuthentication: StreamerSonglistAuthentication?,
) : ApiClient(httpClient, mapper, configuration, defaultAuthentication) {
    fun getPlayHistory(
        request: GetPlayHistoryRequest,
        authentication: StreamerSonglistAuthentication? = null,
    ): PlayHistoryResponseBody {
        return get("/play_history")
            .queryParams(request.queryParams())
            .authentication(requiredAuthentication(authentication))
            .send(responseType())
    }

    fun createPlayHistory(
        request: CreatePlayHistoryRequest,
        authentication: StreamerSonglistAuthentication? = null,
    ): PlayHistoryResponseBody {
        return post("/play_history")
            .body(request)
            .authentication(requiredAuthentication(authentication))
            .send(responseType())
    }

    fun exportPlayHistory(
        lookup: StreamerLookup,
        authentication: StreamerSonglistAuthentication? = null,
    ): ExportPlayHistoryResponseBody {
        return get("/play_history/export")
            .queryParams(lookup.queryParams())
            .authentication(requiredAuthentication(authentication))
            .send(responseType())
    }

    fun getPlayHistoryEntry(
        playHistoryId: PlayHistoryId,
        authentication: StreamerSonglistAuthentication? = null,
    ): PlayHistoryResponseBody {
        return get("/play_history/{play_history_id}")
            .pathParam("play_history_id", playHistoryId)
            .authentication(requiredAuthentication(authentication))
            .send(responseType())
    }

    fun updatePlayHistoryEntry(
        playHistoryId: PlayHistoryId,
        request: UpdatePlayHistoryRequest,
        authentication: StreamerSonglistAuthentication? = null,
    ): PlayHistoryResponseBody {
        return put("/play_history/{play_history_id}")
            .pathParam("play_history_id", playHistoryId)
            .body(request)
            .authentication(requiredAuthentication(authentication))
            .send(responseType())
    }

    fun deletePlayHistoryEntry(
        playHistoryId: PlayHistoryId,
        authentication: StreamerSonglistAuthentication? = null,
    ) {
        delete("/play_history/{play_history_id}")
            .pathParam("play_history_id", playHistoryId)
            .authentication(requiredAuthentication(authentication))
            .sendNoContent()
    }
}

data class GetPlayHistoryRequest(
    val lookup: StreamerLookup,
    val page: CursorPage = CursorPage(),
    val orderBy: PlayHistoryOrderBy? = null,
    val orderDirection: SortDirection? = null,
    val songId: SongId? = null,
    val searchSong: String? = null,
    val searchRequester: String? = null,
    val playedAfter: Instant? = null,
    val playedBefore: Instant? = null,
) {
    internal fun queryParams(): Map<String, Any?> = lookup.queryParams() + page.queryParams() + mapOf(
        "order_by" to orderBy,
        "order_dir" to orderDirection,
        "song_id" to songId,
        "search_song" to searchSong,
        "search_requester" to searchRequester,
        "played_after" to playedAfter,
        "played_before" to playedBefore,
    )
}
