package com.mbayou.streamersonglist4k.events

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.mbayou.streamersonglist4k.StreamerSonglistConfiguration
import com.mbayou.streamersonglist4k.api.QueueId
import com.mbayou.streamersonglist4k.api.StreamerSonglistAuthentication
import com.mbayou.streamersonglist4k.queue.QueueDetails
import java.net.URI
import java.net.http.HttpClient
import java.net.http.WebSocket
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

class EventClient(
    private val httpClient: HttpClient,
    private val mapper: ObjectMapper,
    private val configuration: StreamerSonglistConfiguration,
    private val defaultAuthentication: StreamerSonglistAuthentication?,
) {
    private val parser = EventMessageParser(mapper)

    fun connect(
        channels: Collection<StreamerSonglistChannel>,
        listener: StreamerSonglistEventListener,
        authentication: StreamerSonglistAuthentication? = null,
    ): CompletableFuture<EventSession> {
        val auth = authentication ?: defaultAuthentication ?: error("Authentication is required for the event connection")
        val lifecycle = EventSessionLifecycle()
        val webSocketListener = Listener(parser, listener, lifecycle)
        return httpClient
            .newWebSocketBuilder()
            .buildAsync(URI.create(configuration.eventsBaseUrl), webSocketListener)
            .thenApply { webSocket ->
                val session = EventSession(webSocket, mapper, lifecycle.completion)
                session.connect(auth.value)
                channels.forEach { session.subscribe(it) }
                session
            }
    }

    fun parseMessage(message: String): List<StreamerSonglistEventEnvelope> {
        return parser.parse(message)
    }

    fun parseMessage(root: JsonNode): List<StreamerSonglistEventEnvelope> {
        return parser.parse(root)
    }

}

internal class Listener(
    private val parser: EventMessageParser,
    private val eventListener: StreamerSonglistEventListener,
    private val lifecycle: EventSessionLifecycle,
) : WebSocket.Listener {
    private val buffer = StringBuilder()

    override fun onText(webSocket: WebSocket, data: CharSequence, last: Boolean): CompletionStage<*> {
        buffer.append(data)
        if (last) {
            val message = buffer.toString()
            buffer.clear()
            try {
                parser.parse(message).forEach(eventListener::onEvent)
            } catch (error: Throwable) {
                lifecycle.fail(error)
                webSocket.abort()
                return CompletableFuture<Nothing?>().apply {
                    completeExceptionally(error)
                }
            }
        }
        webSocket.request(1)
        return CompletableFuture.completedFuture(null)
    }

    override fun onOpen(webSocket: WebSocket) {
        webSocket.request(1)
    }

    override fun onClose(webSocket: WebSocket, statusCode: Int, reason: String): CompletionStage<*> {
        lifecycle.complete()
        return CompletableFuture.completedFuture(null)
    }

    override fun onError(webSocket: WebSocket, error: Throwable) {
        lifecycle.fail(error)
    }
}

internal class EventSessionLifecycle {
    val completion: CompletableFuture<Unit> = CompletableFuture()

    fun complete() {
        completion.complete(Unit)
    }

    fun fail(error: Throwable) {
        completion.completeExceptionally(error)
    }
}

internal class EventMessageParser(private val mapper: ObjectMapper) {
    fun parse(message: String): List<StreamerSonglistEventEnvelope> {
        return parse(mapper.readTree(message))
    }

    fun parse(root: JsonNode): List<StreamerSonglistEventEnvelope> {
        val push = root.get("push")
        if (push != null && push.isObject) {
            return listOfNotNull(parsePush(push))
        }

        val type = root.get("type")?.asText()
        if (type != null) {
            return listOf(StreamerSonglistEventEnvelope(channel = null, event = parseEvent(type, root.get("data"))))
        }

        return emptyList()
    }

    private fun parsePush(push: JsonNode): StreamerSonglistEventEnvelope? {
        val channel = push.get("channel")?.asText()?.let { StreamerSonglistChannel.raw(it) }
        val pubData = push.get("pub")?.get("data") ?: return null
        val type = pubData.get("type")?.asText() ?: return null
        return StreamerSonglistEventEnvelope(channel = channel, event = parseEvent(type, pubData.get("data")))
    }

    private fun parseEvent(type: String, payload: JsonNode?): StreamerSonglistEvent {
        return when (type) {
            "queue_add" -> StreamerSonglistEvent.QueueAdd(mapper.treeToValue(unwrapData(payload), QueueDetails::class.java))
            "queue_remove" -> StreamerSonglistEvent.QueueRemove(
                QueueId(requireNotNull(payload?.get("id")?.asInt()) { "queue_remove payload requires id" })
            )
            "queue_update" -> StreamerSonglistEvent.QueueUpdate
            "play_history_add" -> StreamerSonglistEvent.PlayHistoryAdd
            "saved_queue_update" -> StreamerSonglistEvent.SavedQueueUpdate
            else -> StreamerSonglistEvent.Unknown(type, payload)
        }
    }

    private fun unwrapData(payload: JsonNode?): JsonNode {
        return requireNotNull(payload?.get("data") ?: payload) { "Event payload is required" }
    }
}

class EventSession(
    private val webSocket: WebSocket,
    private val mapper: ObjectMapper,
    val completion: CompletableFuture<Unit>,
) {
    private var nextId: Int = 1

    fun connect(token: String): CompletableFuture<WebSocket> {
        return send(
            mapOf(
                "id" to nextId++,
                "connect" to mapOf(
                    "name" to "streamersonglist4k",
                    "token" to token,
                ),
            )
        )
    }

    fun subscribe(channel: StreamerSonglistChannel): CompletableFuture<WebSocket> {
        return send(
            mapOf(
                "id" to nextId++,
                "subscribe" to mapOf("channel" to channel.value),
            )
        )
    }

    fun close(statusCode: Int = WebSocket.NORMAL_CLOSURE, reason: String = "closed"): CompletableFuture<WebSocket> {
        return webSocket.sendClose(statusCode, reason)
    }

    private fun send(payload: Map<String, Any>): CompletableFuture<WebSocket> {
        return webSocket.sendText(mapper.writeValueAsString(payload), true)
    }
}
