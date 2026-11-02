package com.mbayou.streamersonglist4k.events

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.mbayou.streamersonglist4k.StreamerSonglistConfiguration
import com.mbayou.streamersonglist4k.api.QueueId
import java.net.http.HttpClient
import java.net.http.WebSocket
import java.util.concurrent.CompletionException
import java.util.concurrent.CompletableFuture
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EventClientTest {
    private val mapper: ObjectMapper = ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(JavaTimeModule())
        .registerModule(
            KotlinModule.Builder()
                .configure(KotlinFeature.NullIsSameAsDefault, true)
                .build()
        )

    private val client = EventClient(
        HttpClient.newHttpClient(),
        mapper,
        StreamerSonglistConfiguration.builder()
            .clientId("client-id")
            .clientSecret("client-secret")
            .redirectUri("https://app.example/callback")
            .staging()
            .build(),
        defaultAuthentication = null,
    )

    @Test
    fun `OAuth event connections authenticate the handshake with bearer and matching client id`() {
        val http = Mockito.mock(HttpClient::class.java)
        val builder = Mockito.mock(WebSocket.Builder::class.java, Mockito.RETURNS_SELF)
        Mockito.`when`(http.newWebSocketBuilder()).thenReturn(builder)
        val opened = CompletableFuture<WebSocket>()
        Mockito.`when`(builder.buildAsync(Mockito.any(), Mockito.any())).thenReturn(opened)
        val configuration = StreamerSonglistConfiguration.builder().clientId("client-id").clientSecret("secret")
            .redirectUri("https://example.com").build()
        val connecting = EventClient(http, mapper, configuration, null).connect(emptyList(), StreamerSonglistEventListener {},
            com.mbayou.streamersonglist4k.api.StreamerSonglistAuthentication.OAuthAccessToken("opaque-access"))
        Mockito.verify(builder).header("Authorization", "Bearer opaque-access")
        Mockito.verify(builder).header("Client-Id", "client-id")
        connecting.cancel(true)
        val lateSocket = mockWebSocket()
        opened.complete(lateSocket)
        Mockito.verify(lateSocket).abort()
    }

    @Test
    fun `local close completes lifecycle without waiting for a remote close callback`() {
        val lifecycle = EventSessionLifecycle()
        val protocol = EventSessionProtocol(mapper, EventMessageParser(mapper), StreamerSonglistEventListener {}, lifecycle)
        val socket = mockWebSocket()
        protocol.attach(socket).close(reason = "cancelled")
        assertTrue(lifecycle.completion.isDone)
    }

    @Test
    fun `current invalidation events and null payloads preserve the connection`() {
        val types = listOf("queue_add", "queue_remove", "queue_update", "queue_clear", "queue_reorder",
            "now_playing_update", "play_history_remove", "streamer_settings_update")
        for (type in types) {
            val event = client.parseMessage("""{"type":"$type","data":null}""").single().event
            assertEquals(type, event.type)
            assertFalse(event is StreamerSonglistEvent.Unknown)
        }
        assertEquals("streamer:7-saved_queue", StreamerSonglistChannel.savedQueue(com.mbayou.streamersonglist4k.api.StreamerId(7)).value)
    }

    @Test
    fun `now playing removal and complete queue reordering decode documented shapes`() {
        val playing = assertIs<StreamerSonglistEvent.NowPlayingUpdate>(client.parseMessage(
            """{"type":"now_playing_update","data":{"streamerId":7,"playing":null,"previousId":42}}""").single().event)
        assertEquals(QueueId(42), playing.update?.previousId)
        val reorder = assertIs<StreamerSonglistEvent.QueueReorder>(client.parseMessage(
            """{"type":"queue_reorder","data":{"streamerId":7,"playingId":null,"items":[{"id":42,"position":1}]}}""").single().event)
        assertEquals(QueueId(42), reorder.order?.items?.single()?.id)
    }

    @Test
    fun `unacknowledged commands time out and fail the event session`() {
        val socket = Mockito.mock(WebSocket::class.java)
        Mockito.`when`(socket.sendText(Mockito.anyString(), Mockito.eq(true))).thenReturn(CompletableFuture.completedFuture(socket))
        val lifecycle = EventSessionLifecycle()
        val protocol = EventSessionProtocol(mapper, EventMessageParser(mapper), StreamerSonglistEventListener {}, lifecycle,
            java.time.Duration.ofMillis(30))
        val session = protocol.attach(socket)
        assertFailsWith<java.util.concurrent.ExecutionException> { session.connect().get(2, java.util.concurrent.TimeUnit.SECONDS) }
        assertTrue(lifecycle.completion.isCompletedExceptionally)
    }

    @Test
    fun `parseMessage parses Centrifugo queue remove events`() {
        val message = """
            {
              "push": {
                "channel": "streamer:7-queue",
                "pub": {
                  "data": {
                    "type": "queue_remove",
                    "data": {"id": 42}
                  }
                }
              }
            }
        """.trimIndent()

        val envelope = client.parseMessage(message).single()
        val event = assertIs<StreamerSonglistEvent.QueueRemove>(envelope.event)

        assertEquals(StreamerSonglistChannel.queue(com.mbayou.streamersonglist4k.api.StreamerId(7)), envelope.channel)
        assertEquals(QueueId(42), event.queueId)
    }

    @Test
    fun `parseMessage preserves unknown event payloads`() {
        val message = """{"type":"future_event","data":{"value":1}}"""

        val event = assertIs<StreamerSonglistEvent.Unknown>(client.parseMessage(message).single().event)

        assertEquals("future_event", event.type)
        assertEquals(1, event.payload?.get("value")?.asInt())
    }

    @Test
    fun `parseMessage supports line-delimited event batches`() {
        val message = """
            {"type":"future_event","data":{"value":1}}
            {"type":"future_event","data":{"value":2}}
        """.trimIndent()

        val events = client.parseMessage(message).map { assertIs<StreamerSonglistEvent.Unknown>(it.event) }

        assertEquals(listOf(1, 2), events.map { it.payload?.get("value")?.asInt() })
    }

    @Test
    fun `listener completes session lifecycle on local close`() {
        val lifecycle = EventSessionLifecycle()
        val protocol = EventSessionProtocol(
            mapper = mapper,
            parser = EventMessageParser(mapper),
            eventListener = StreamerSonglistEventListener { },
            lifecycle = lifecycle,
        )
        val webSocket = mockWebSocket()
        val listener = Listener(protocol)

        protocol.attach(webSocket).close(reason = "closed")
        listener.onClose(webSocket, WebSocket.NORMAL_CLOSURE, "closed")

        assertTrue(lifecycle.completion.isDone)
        lifecycle.completion.join()
    }

    @Test
    fun `listener fails session lifecycle on remote close`() {
        val lifecycle = EventSessionLifecycle()
        val protocol = EventSessionProtocol(
            mapper = mapper,
            parser = EventMessageParser(mapper),
            eventListener = StreamerSonglistEventListener { },
            lifecycle = lifecycle,
        )
        val webSocket = mockWebSocket()
        val listener = Listener(protocol)

        protocol.attach(webSocket)
        listener.onClose(webSocket, 3500, "permission denied")

        assertTrue(lifecycle.completion.isCompletedExceptionally)
        val error = assertFailsWith<CompletionException> {
            lifecycle.completion.join()
        }
        val cause = assertIs<EventSessionClosedException>(error.cause)
        assertEquals(3500, cause.statusCode)
        assertEquals("permission denied", cause.closeReason)
        assertFalse(cause.initiatedLocally)
    }

    @Test
    fun `listener fails session lifecycle when event parsing throws`() {
        val lifecycle = EventSessionLifecycle()
        val protocol = EventSessionProtocol(
            mapper = mapper,
            parser = EventMessageParser(mapper),
            eventListener = StreamerSonglistEventListener {
                error("boom")
            },
            lifecycle = lifecycle,
        )
        val webSocket = Mockito.mock(WebSocket::class.java)
        val listener = Listener(protocol)

        protocol.attach(webSocket)

        listener.onText(
            webSocket,
            """{"type":"future_event","data":{"value":1}}""",
            true
        )

        assertTrue(lifecycle.completion.isCompletedExceptionally)
        assertFailsWith<CompletionException> {
            lifecycle.completion.join()
        }
    }

    @Test
    fun `connect future completes only after command acknowledgement`() {
        val lifecycle = EventSessionLifecycle()
        val protocol = EventSessionProtocol(
            mapper = mapper,
            parser = EventMessageParser(mapper),
            eventListener = StreamerSonglistEventListener { },
            lifecycle = lifecycle,
        )
        val webSocket = mockWebSocket()
        val listener = Listener(protocol)
        val session = protocol.attach(webSocket)

        val connectFuture = session.connect("token")

        assertFalse(connectFuture.isDone)

        listener.onText(
            webSocket,
            """{"id":1,"connect":{"client":"abc","ping":25,"pong":true}}""",
            true
        )

        assertTrue(connectFuture.isDone)
        connectFuture.join()
    }

    @Test
    fun `subscribe future fails with command error reply`() {
        val lifecycle = EventSessionLifecycle()
        val protocol = EventSessionProtocol(
            mapper = mapper,
            parser = EventMessageParser(mapper),
            eventListener = StreamerSonglistEventListener { },
            lifecycle = lifecycle,
        )
        val webSocket = mockWebSocket()
        val listener = Listener(protocol)
        val session = protocol.attach(webSocket)

        val subscribeFuture = session.subscribe(StreamerSonglistChannel.raw("streamer:7-queue"))

        listener.onText(
            webSocket,
            """{"id":1,"error":{"code":103,"message":"permission denied","temporary":false}}""",
            true
        )

        val error = assertFailsWith<CompletionException> {
            subscribeFuture.join()
        }
        val cause = assertIs<EventSessionCommandException>(error.cause)
        assertEquals("subscribe", cause.commandName)
        assertEquals(103, cause.code)
    }

    @Test
    fun `listener replies to server ping frames`() {
        val lifecycle = EventSessionLifecycle()
        val protocol = EventSessionProtocol(
            mapper = mapper,
            parser = EventMessageParser(mapper),
            eventListener = StreamerSonglistEventListener { },
            lifecycle = lifecycle,
        )
        val webSocket = mockWebSocket()
        val listener = Listener(protocol)

        protocol.attach(webSocket)
        listener.onText(webSocket, "{}", true)

        Mockito.verify(webSocket).sendText("{}", true)
    }

    @Test
    fun `anonymous connect omits token from connect payload`() {
        val lifecycle = EventSessionLifecycle()
        val protocol = EventSessionProtocol(
            mapper = mapper,
            parser = EventMessageParser(mapper),
            eventListener = StreamerSonglistEventListener { },
            lifecycle = lifecycle,
        )
        val webSocket = mockWebSocket()
        val session = protocol.attach(webSocket)
        val payloadCaptor = ArgumentCaptor.forClass(String::class.java)

        session.connect()

        Mockito.verify(webSocket).sendText(payloadCaptor.capture(), Mockito.eq(true))
        assertFalse(payloadCaptor.value.contains("\"token\""))
        assertTrue(payloadCaptor.value.contains("\"connect\""))
    }

    private fun mockWebSocket(): WebSocket {
        val webSocket = Mockito.mock(WebSocket::class.java)
        Mockito.`when`(webSocket.sendText(Mockito.anyString(), Mockito.eq(true)))
            .thenReturn(CompletableFuture.completedFuture(webSocket))
        Mockito.`when`(webSocket.sendClose(Mockito.anyInt(), Mockito.anyString()))
            .thenReturn(CompletableFuture.completedFuture(webSocket))
        return webSocket
    }
}
