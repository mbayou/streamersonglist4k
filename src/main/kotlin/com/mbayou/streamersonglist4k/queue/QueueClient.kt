package com.mbayou.streamersonglist4k.queue

import com.fasterxml.jackson.databind.ObjectMapper
import com.mbayou.streamersonglist4k.StreamerSonglistConfiguration
import com.mbayou.streamersonglist4k.api.ApiClient
import com.mbayou.streamersonglist4k.api.QueueId
import com.mbayou.streamersonglist4k.api.RequestId
import com.mbayou.streamersonglist4k.api.StreamerId
import com.mbayou.streamersonglist4k.api.StreamerLookup
import com.mbayou.streamersonglist4k.api.StreamerSonglistAuthentication
import com.mbayou.streamersonglist4k.playhistory.HistoryDetails
import java.net.http.HttpClient

class QueueClient(
    httpClient: HttpClient,
    mapper: ObjectMapper,
    configuration: StreamerSonglistConfiguration,
    defaultAuthentication: StreamerSonglistAuthentication?,
) : ApiClient(httpClient, mapper, configuration, defaultAuthentication) {
    fun getQueue(
        lookup: StreamerLookup,
        authentication: StreamerSonglistAuthentication? = null,
    ): QueueResponseBody {
        return get("/queue")
            .queryParams(lookup.queryParams())
            .authentication(authentication ?: defaultAuthentication)
            .send(responseType())
    }

    fun createQueue(
        request: CreateQueueRequest,
        authentication: StreamerSonglistAuthentication? = null,
    ): QueueResponseBody {
        return post("/queue")
            .body(request)
            .authentication(requiredAuthentication(authentication))
            .send(responseType())
    }

    fun clearQueue(
        lookup: StreamerLookup,
        authentication: StreamerSonglistAuthentication? = null,
    ): ClearQueueResponseBody {
        return delete("/queue")
            .queryParams(lookup.queryParams())
            .authentication(requiredAuthentication(authentication))
            .send(responseType())
    }

    fun markPlayed(
        queueId: QueueId? = null,
        position: Int? = null,
        streamerId: StreamerId? = null,
        authentication: StreamerSonglistAuthentication? = null,
    ): HistoryDetails {
        return post("/queue/played")
            .queryParams(
                mapOf(
                    "queue_id" to queueId,
                    "position" to position,
                    "streamer_id" to streamerId,
                )
            )
            .authentication(requiredAuthentication(authentication))
            .send(responseType())
    }

    fun deleteQueue(
        queueId: QueueId,
        authentication: StreamerSonglistAuthentication? = null,
    ) {
        delete("/queue/{queueId}")
            .pathParam("queueId", queueId)
            .authentication(requiredAuthentication(authentication))
            .sendNoContent()
    }

    fun updateQueue(
        queueId: QueueId,
        request: UpdateQueueRequest,
        authentication: StreamerSonglistAuthentication? = null,
    ): QueueResponseBody {
        return patch("/queue/{queueId}")
            .pathParam("queueId", queueId)
            .body(request)
            .authentication(requiredAuthentication(authentication))
            .send(responseType())
    }

    fun addRequest(
        queueId: QueueId,
        request: AddQueueRequestRequest,
        authentication: StreamerSonglistAuthentication? = null,
    ): QueueDetails {
        return post("/queue/{queueId}/requests")
            .pathParam("queueId", queueId)
            .body(request)
            .authentication(requiredAuthentication(authentication))
            .send(responseType())
    }

    fun deleteRequest(
        queueId: QueueId,
        requestId: RequestId,
        authentication: StreamerSonglistAuthentication? = null,
    ): DeleteQueueRequestResponseBody {
        return delete("/queue/{queueId}/requests/{requestId}")
            .pathParam("queueId", queueId)
            .pathParam("requestId", requestId)
            .authentication(requiredAuthentication(authentication))
            .send(responseType())
    }

    fun updateRequest(
        queueId: QueueId,
        requestId: RequestId,
        request: UpdateQueueRequestRequest,
        authentication: StreamerSonglistAuthentication? = null,
    ): QueueDetails {
        return patch("/queue/{queueId}/requests/{requestId}")
            .pathParam("queueId", queueId)
            .pathParam("requestId", requestId)
            .body(request)
            .authentication(requiredAuthentication(authentication))
            .send(responseType())
    }
}
