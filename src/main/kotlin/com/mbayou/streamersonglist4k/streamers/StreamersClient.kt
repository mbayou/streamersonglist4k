package com.mbayou.streamersonglist4k.streamers

import com.fasterxml.jackson.databind.ObjectMapper
import com.mbayou.streamersonglist4k.StreamerSonglistConfiguration
import com.mbayou.streamersonglist4k.api.ApiClient
import com.mbayou.streamersonglist4k.api.StreamerLookup
import com.mbayou.streamersonglist4k.api.StreamerSonglistAuthentication
import java.net.http.HttpClient

class StreamersClient(
    httpClient: HttpClient,
    mapper: ObjectMapper,
    configuration: StreamerSonglistConfiguration,
    defaultAuthentication: StreamerSonglistAuthentication?,
) : ApiClient(httpClient, mapper, configuration, defaultAuthentication) {
    fun getStreamer(
        lookup: StreamerLookup,
        authentication: StreamerSonglistAuthentication? = null,
    ): StreamerDetails {
        return get("/streamers")
            .queryParams(lookup.queryParams())
            .authentication(requiredAuthentication(authentication))
            .send(responseType())
    }
}
