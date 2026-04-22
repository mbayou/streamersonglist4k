package com.mbayou.streamersonglist4k

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.mbayou.streamersonglist4k.api.StreamerSonglistAuthentication
import com.mbayou.streamersonglist4k.attributes.AttributesClient
import com.mbayou.streamersonglist4k.authorization.AuthorizationClient
import com.mbayou.streamersonglist4k.events.EventClient
import com.mbayou.streamersonglist4k.playhistory.PlayHistoryClient
import com.mbayou.streamersonglist4k.queue.QueueClient
import com.mbayou.streamersonglist4k.songs.SongsClient
import com.mbayou.streamersonglist4k.streamers.StreamersClient
import com.mbayou.streamersonglist4k.users.UsersClient
import java.net.http.HttpClient

class StreamerSonglistClient(
    configuration: StreamerSonglistConfiguration,
    private val defaultAuthentication: StreamerSonglistAuthentication? = null,
) {
    private val httpClient = HttpClient.newHttpClient()
    private val objectMapper: ObjectMapper = ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(JavaTimeModule())
        .registerModule(
            KotlinModule.Builder()
                .configure(KotlinFeature.NullIsSameAsDefault, true)
                .build()
        )

    private val authorizationClient = AuthorizationClient(httpClient, objectMapper, configuration)
    private val streamersClient = StreamersClient(httpClient, objectMapper, configuration, defaultAuthentication)
    private val usersClient = UsersClient(httpClient, objectMapper, configuration, defaultAuthentication)
    private val queueClient = QueueClient(httpClient, objectMapper, configuration, defaultAuthentication)
    private val songsClient = SongsClient(httpClient, objectMapper, configuration, defaultAuthentication)
    private val attributesClient = AttributesClient(httpClient, objectMapper, configuration, defaultAuthentication)
    private val playHistoryClient = PlayHistoryClient(httpClient, objectMapper, configuration, defaultAuthentication)
    private val eventClient = EventClient(httpClient, objectMapper, configuration, defaultAuthentication)

    fun authorization(): AuthorizationClient = authorizationClient
    fun streamers(): StreamersClient = streamersClient
    fun users(): UsersClient = usersClient
    fun queue(): QueueClient = queueClient
    fun songs(): SongsClient = songsClient
    fun attributes(): AttributesClient = attributesClient
    fun playHistory(): PlayHistoryClient = playHistoryClient
    fun events(): EventClient = eventClient
}
