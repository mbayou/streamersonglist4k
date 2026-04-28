package com.mbayou.streamersonglist4k.events

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.JsonNode
import com.mbayou.streamersonglist4k.api.QueueId
import com.mbayou.streamersonglist4k.api.StreamerId
import com.mbayou.streamersonglist4k.queue.QueueDetails

@JvmInline
value class StreamerSonglistChannel @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(
    @get:JsonValue val value: String,
) {
    companion object {
        fun all(streamerId: StreamerId): StreamerSonglistChannel =
            StreamerSonglistChannel("streamer:${streamerId.value}")

        fun queue(streamerId: StreamerId): StreamerSonglistChannel =
            StreamerSonglistChannel("streamer:${streamerId.value}-queue")

        fun song(streamerId: StreamerId): StreamerSonglistChannel =
            StreamerSonglistChannel("streamer:${streamerId.value}-song")

        fun playHistory(streamerId: StreamerId): StreamerSonglistChannel =
            StreamerSonglistChannel("streamer:${streamerId.value}-play_history")

        fun savedQueue(streamerId: StreamerId): StreamerSonglistChannel =
            StreamerSonglistChannel("streamer:${streamerId.value}-saved-queue")

        fun admin(streamerId: StreamerId): StreamerSonglistChannel =
            StreamerSonglistChannel("streamer-admin:${streamerId.value}")

        fun raw(value: String): StreamerSonglistChannel = StreamerSonglistChannel(value)
    }
}

data class StreamerSonglistEventEnvelope(
    val channel: StreamerSonglistChannel?,
    val event: StreamerSonglistEvent,
)

sealed interface StreamerSonglistEvent {
    val type: String

    data class QueueAdd(
        val queue: QueueDetails,
    ) : StreamerSonglistEvent {
        override val type: String = "queue_add"
    }

    data class QueueRemove(
        val queueId: QueueId,
    ) : StreamerSonglistEvent {
        override val type: String = "queue_remove"
    }

    data object QueueUpdate : StreamerSonglistEvent {
        override val type: String = "queue_update"
    }

    data object PlayHistoryAdd : StreamerSonglistEvent {
        override val type: String = "play_history_add"
    }

    data object SavedQueueUpdate : StreamerSonglistEvent {
        override val type: String = "saved_queue_update"
    }

    data class Unknown(
        override val type: String,
        val payload: JsonNode?,
    ) : StreamerSonglistEvent
}

fun interface StreamerSonglistEventListener {
    fun onEvent(envelope: StreamerSonglistEventEnvelope)
}
