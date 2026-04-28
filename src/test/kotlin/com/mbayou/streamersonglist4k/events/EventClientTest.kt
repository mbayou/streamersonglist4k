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
import org.mockito.Mockito
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
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
    fun `listener completes session lifecycle on close`() {
        val lifecycle = EventSessionLifecycle()
        val listener = Listener(
            parser = EventMessageParser(mapper),
            eventListener = StreamerSonglistEventListener { },
            lifecycle = lifecycle,
        )

        listener.onClose(Mockito.mock(WebSocket::class.java), WebSocket.NORMAL_CLOSURE, "closed")

        assertTrue(lifecycle.completion.isDone)
        lifecycle.completion.join()
    }

    @Test
    fun `listener fails session lifecycle when event parsing throws`() {
        val lifecycle = EventSessionLifecycle()
        val listener = Listener(
            parser = EventMessageParser(mapper),
            eventListener = StreamerSonglistEventListener {
                error("boom")
            },
            lifecycle = lifecycle,
        )
        val webSocket = Mockito.mock(WebSocket::class.java)

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
}
