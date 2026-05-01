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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

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
        val protocol = EventSessionProtocol(mapper, parser, listener, lifecycle)
        val webSocketListener = Listener(protocol)
        return httpClient
            .newWebSocketBuilder()
            .buildAsync(URI.create(configuration.eventsBaseUrl), webSocketListener)
            .thenCompose { webSocket ->
                val session = protocol.attach(webSocket)
                session.connect(auth.value)
                    .thenCompose {
                        channels.fold(CompletableFuture.completedFuture(Unit)) { completion, channel ->
                            completion.thenCompose { session.subscribe(channel) }
                        }
                    }
                    .thenApply { session }
                    .whenComplete { _, error ->
                        if (error != null) {
                            session.abort()
                        }
                    }
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
    private val protocol: EventSessionProtocol,
) : WebSocket.Listener {
    private val buffer = StringBuilder()

    override fun onText(webSocket: WebSocket, data: CharSequence, last: Boolean): CompletionStage<*> {
        buffer.append(data)
        if (last) {
            val message = buffer.toString()
            buffer.clear()
            try {
                protocol.handleIncomingMessage(message)
            } catch (error: Throwable) {
                protocol.handleError(error)
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
        protocol.handleClose(statusCode, reason)
        return CompletableFuture.completedFuture(null)
    }

    override fun onError(webSocket: WebSocket, error: Throwable) {
        protocol.handleError(error)
    }
}

internal class EventSessionProtocol(
    private val mapper: ObjectMapper,
    private val parser: EventMessageParser,
    private val eventListener: StreamerSonglistEventListener,
    private val lifecycle: EventSessionLifecycle,
) {
    private val nextId = AtomicInteger(1)
    private val pendingCommands = ConcurrentHashMap<Int, PendingCommand>()
    private val closeRequested = AtomicBoolean(false)

    @Volatile
    private var webSocket: WebSocket? = null

    fun attach(webSocket: WebSocket): EventSession {
        this.webSocket = webSocket
        return EventSession(this, lifecycle.completion)
    }

    fun connect(token: String): CompletableFuture<Unit> {
        return sendCommand(
            commandName = "connect",
            commandPayload = mapOf(
                "name" to "streamersonglist4k",
                "token" to token,
            ),
        )
    }

    fun subscribe(channel: StreamerSonglistChannel): CompletableFuture<Unit> {
        return sendCommand(
            commandName = "subscribe",
            commandPayload = mapOf("channel" to channel.value),
        )
    }

    fun close(statusCode: Int, reason: String): CompletableFuture<WebSocket> {
        closeRequested.set(true)
        return requireWebSocket().sendClose(statusCode, reason)
    }

    fun abort() {
        closeRequested.set(true)
        webSocket?.abort()
    }

    fun handleIncomingMessage(message: String) {
        decodeFrames(message).forEach(::handleFrame)
    }

    fun handleClose(statusCode: Int, reason: String) {
        val closeException = EventSessionClosedException(
            statusCode = statusCode,
            closeReason = reason,
            initiatedLocally = closeRequested.get(),
        )
        failPendingCommands(closeException)
        if (closeRequested.get()) {
            lifecycle.complete()
        } else {
            lifecycle.fail(closeException)
        }
    }

    fun handleError(error: Throwable) {
        failPendingCommands(error)
        lifecycle.fail(error)
    }

    private fun handleFrame(frame: JsonNode) {
        if (frame.isObject && frame.size() == 0) {
            requireWebSocket().sendText("{}", true)
            return
        }

        val replyId = frame.get("id")?.asInt(0) ?: 0
        if (replyId > 0) {
            handleReply(frame, replyId)
            return
        }

        parser.parse(frame).forEach(eventListener::onEvent)
    }

    private fun handleReply(frame: JsonNode, replyId: Int) {
        val pendingCommand = pendingCommands.remove(replyId) ?: return
        val errorNode = frame.get("error")
        if (errorNode != null && !errorNode.isNull) {
            val exception = EventSessionCommandException(
                commandName = pendingCommand.commandName,
                commandId = replyId,
                code = errorNode.get("code")?.asInt(),
                serverMessage = errorNode.get("message")?.asText(),
                temporary = errorNode.get("temporary")?.asBoolean(),
                rawReply = frame.toString(),
            )
            pendingCommand.completion.completeExceptionally(exception)
            lifecycle.fail(exception)
            return
        }
        pendingCommand.completion.complete(Unit)
    }

    private fun sendCommand(
        commandName: String,
        commandPayload: Map<String, Any>,
    ): CompletableFuture<Unit> {
        val replyId = nextId.getAndIncrement()
        val replyFuture = CompletableFuture<Unit>()
        pendingCommands[replyId] = PendingCommand(commandName, replyFuture)

        requireWebSocket()
            .sendText(
                mapper.writeValueAsString(
                    mapOf(
                        "id" to replyId,
                        commandName to commandPayload,
                    )
                ),
                true,
            )
            .whenComplete { _, error ->
                if (error != null) {
                    pendingCommands.remove(replyId)
                    replyFuture.completeExceptionally(error)
                    lifecycle.fail(error)
                }
            }

        return replyFuture
    }

    private fun decodeFrames(message: String): List<JsonNode> {
        return decodeJsonFrames(mapper, message)
    }

    private fun failPendingCommands(error: Throwable) {
        pendingCommands.values.forEach { pending ->
            pending.completion.completeExceptionally(error)
        }
        pendingCommands.clear()
    }

    private fun requireWebSocket(): WebSocket {
        return requireNotNull(webSocket) { "Event session is not attached to a WebSocket yet" }
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
        return decodeJsonFrames(mapper, message)
            .asSequence()
            .flatMap { parse(it).asSequence() }
            .toList()
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

class EventSession internal constructor(
    private val protocol: EventSessionProtocol,
    val completion: CompletableFuture<Unit>,
) {
    fun connect(token: String): CompletableFuture<Unit> {
        return protocol.connect(token)
    }

    fun subscribe(channel: StreamerSonglistChannel): CompletableFuture<Unit> {
        return protocol.subscribe(channel)
    }

    fun close(statusCode: Int = WebSocket.NORMAL_CLOSURE, reason: String = "closed"): CompletableFuture<WebSocket> {
        return protocol.close(statusCode, reason)
    }

    internal fun abort() {
        protocol.abort()
    }
}

internal data class PendingCommand(
    val commandName: String,
    val completion: CompletableFuture<Unit>,
)

class EventSessionCommandException(
    val commandName: String,
    val commandId: Int,
    val code: Int?,
    val serverMessage: String?,
    val temporary: Boolean?,
    val rawReply: String,
) : RuntimeException(
    buildString {
        append("StreamerSonglist event command ")
        append(commandName)
        append(" (id=")
        append(commandId)
        append(") failed")
        if (code != null) {
            append(" with code ")
            append(code)
        }
        if (!serverMessage.isNullOrBlank()) {
            append(": ")
            append(serverMessage)
        }
    }
)

class EventSessionClosedException(
    val statusCode: Int,
    val closeReason: String,
    val initiatedLocally: Boolean,
) : RuntimeException(
    if (initiatedLocally) {
        "StreamerSonglist event session closed locally (code=$statusCode, reason=$closeReason)"
    } else {
        "StreamerSonglist event session closed by server (code=$statusCode, reason=$closeReason)"
    }
)

internal fun decodeJsonFrames(mapper: ObjectMapper, message: String): List<JsonNode> {
    return mapper.readerFor(JsonNode::class.java).readValues<JsonNode>(message).readAll()
}
