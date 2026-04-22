package com.mbayou.streamersonglist4k

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.mbayou.streamersonglist4k.api.QueueId
import com.mbayou.streamersonglist4k.api.SongId
import com.mbayou.streamersonglist4k.api.StreamerId
import com.mbayou.streamersonglist4k.queue.CreateQueueRequest
import com.mbayou.streamersonglist4k.queue.QueueDetails
import com.mbayou.streamersonglist4k.queue.QueueInsertMethod
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class JsonModelTest {
    private val mapper: ObjectMapper = ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(JavaTimeModule())
        .registerModule(
            KotlinModule.Builder()
                .configure(KotlinFeature.NullIsSameAsDefault, true)
                .build()
        )

    @Test
    fun `queue details deserialize documented value-class ids`() {
        val json = """
            {
              "id": 44,
              "createdAt": "2026-04-22T10:15:30Z",
              "songId": 123,
              "nonlistSong": null,
              "note": null,
              "streamerId": 7,
              "position": 1,
              "song": {
                "title": "Song",
                "artist": "Artist",
                "lastPlayed": null,
                "lastPlayedFrom": "",
                "timesPlayed": 2,
                "comment": null,
                "capo": null,
                "attributes": [{"name": "hype", "image": null}]
              },
              "requests": null
            }
        """.trimIndent()

        val details = mapper.readValue(json, QueueDetails::class.java)

        assertEquals(QueueId(44), details.id)
        assertEquals(123, details.songId?.value)
        assertEquals(7, details.streamerId.value)
        assertEquals("Song", details.song.title)
        assertEquals("hype", details.song.attributes?.first()?.name)
        assertNull(details.requests)
    }

    @Test
    fun `request bodies serialize value-class ids and api enums as scalar values`() {
        val request = CreateQueueRequest(
            streamerId = StreamerId(7),
            songId = SongId(123),
            insertMethod = QueueInsertMethod.END,
        )

        val json = mapper.writeValueAsString(request)
        val node = mapper.readTree(json)

        assertEquals(7, node.get("streamerId").asInt())
        assertEquals(123, node.get("songId").asInt())
        assertEquals("end", node.get("insertMethod").asText())
    }
}
