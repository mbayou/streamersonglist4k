package com.mbayou.streamersonglist4k.attributes

import com.fasterxml.jackson.databind.ObjectMapper
import com.mbayou.streamersonglist4k.StreamerSonglistConfiguration
import com.mbayou.streamersonglist4k.api.ApiClient
import com.mbayou.streamersonglist4k.api.AttributeId
import com.mbayou.streamersonglist4k.api.StreamerLookup
import com.mbayou.streamersonglist4k.api.StreamerSonglistAuthentication
import java.net.http.HttpClient

class AttributesClient(
    httpClient: HttpClient,
    mapper: ObjectMapper,
    configuration: StreamerSonglistConfiguration,
    defaultAuthentication: StreamerSonglistAuthentication?,
) : ApiClient(httpClient, mapper, configuration, defaultAuthentication) {
    fun getAttributes(
        lookup: StreamerLookup,
        includeSongCounts: Boolean? = null,
        authentication: StreamerSonglistAuthentication? = null,
    ): GetAttributesResponseBody {
        return get("/attributes")
            .queryParams(lookup.queryParams() + mapOf("include_song_counts" to includeSongCounts))
            .authentication(requiredAuthentication(authentication))
            .send(responseType())
    }

    fun createAttribute(
        request: CreateAttributeRequest,
        authentication: StreamerSonglistAuthentication? = null,
    ): Attribute {
        return post("/attributes")
            .body(request)
            .authentication(requiredAuthentication(authentication))
            .send(responseType())
    }

    fun updateAttribute(
        attributeId: AttributeId,
        request: UpdateAttributeRequest,
        authentication: StreamerSonglistAuthentication? = null,
    ): Attribute {
        return patch("/attributes/{attributeId}")
            .pathParam("attributeId", attributeId)
            .body(request)
            .authentication(requiredAuthentication(authentication))
            .send(responseType())
    }

    fun deleteAttribute(
        attributeId: AttributeId,
        authentication: StreamerSonglistAuthentication? = null,
    ) {
        delete("/attributes/{attributeId}")
            .pathParam("attributeId", attributeId)
            .authentication(requiredAuthentication(authentication))
            .sendNoContent()
    }
}
