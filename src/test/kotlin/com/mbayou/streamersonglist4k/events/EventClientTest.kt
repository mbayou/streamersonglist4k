package com.mbayou.streamersonglist4k.events

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.mbayou.streamersonglist4k.StreamerSonglistConfiguration
import com.mbayou.streamersonglist4k.api.QueueId
import java.net.http.HttpClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

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
}
