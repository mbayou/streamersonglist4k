# streamersonglist4k

Kotlin/JVM client for the documented StreamerSonglist API.

This library targets the StreamerSonglist V2 API described by:

- https://dev.staging.streamersonglist.com/docs/overview
- https://api.staging.streamersonglist.com/openapi.json

## Install

Published builds are intended to be consumed through JitPack:

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.mbayou:streamersonglist4k:0.1.0")
}
```

## Configure

```kotlin
import com.mbayou.streamersonglist4k.StreamerSonglistClient
import com.mbayou.streamersonglist4k.StreamerSonglistConfiguration
import com.mbayou.streamersonglist4k.api.StreamerSonglistAuthentication

val configuration = StreamerSonglistConfiguration.builder()
    .clientId(System.getenv("SSL_CLIENT_ID"))
    .clientSecret(System.getenv("SSL_CLIENT_SECRET"))
    .redirectUri("https://example.com/oauth/streamersonglist/callback")
    .build()

val client = StreamerSonglistClient(
    configuration,
    defaultAuthentication = StreamerSonglistAuthentication.OAuthAccessToken("<access-token>")
)
```

For staging:

```kotlin
val configuration = StreamerSonglistConfiguration.builder()
    .clientId("client-id")
    .clientSecret("client-secret")
    .redirectUri("https://example.com/callback")
    .staging()
    .build()
```

## OAuth

```kotlin
import com.mbayou.streamersonglist4k.authorization.ScopePermission
import com.mbayou.streamersonglist4k.authorization.ScopeResource
import com.mbayou.streamersonglist4k.authorization.StreamerSonglistScope

val authorizationUrl = client.authorization().authorizationUrl(
    scopes = listOf(
        StreamerSonglistScope.resource(ScopeResource.STREAMER_QUEUE, ScopePermission.READ),
        StreamerSonglistScope.wildcard(ScopeResource.STREAMER_SONG)
    ),
    state = "csrf-token"
)

val token = client.authorization().exchangeAuthorizationCode(code)
```

Streamer access tokens are also supported:

```kotlin
val client = StreamerSonglistClient(
    configuration,
    defaultAuthentication = StreamerSonglistAuthentication.StreamerAccessToken("<streamer-token>")
)
```

## Queue

```kotlin
import com.mbayou.streamersonglist4k.api.StreamerId
import com.mbayou.streamersonglist4k.api.StreamerLookup
import com.mbayou.streamersonglist4k.queue.CreateQueueRequest
import com.mbayou.streamersonglist4k.queue.QueueInsertMethod

val lookup = StreamerLookup.ById(StreamerId(123))
val queue = client.queue().getQueue(lookup)

client.queue().createQueue(
    CreateQueueRequest(
        streamerId = StreamerId(123),
        query = "Never Gonna Give You Up",
        insertMethod = QueueInsertMethod.END
    )
)
```

## Songs And Attributes

```kotlin
import com.mbayou.streamersonglist4k.api.AttributeId
import com.mbayou.streamersonglist4k.api.CursorPage
import com.mbayou.streamersonglist4k.songs.GetSongsRequest
import com.mbayou.streamersonglist4k.songs.InQueueFilter

val songs = client.songs().getSongs(
    GetSongsRequest(
        lookup = StreamerLookup.ById(StreamerId(123)),
        attributes = listOf(AttributeId(10)),
        inQueue = InQueueFilter.EXCLUDE,
        page = CursorPage(limit = 25)
    )
)

val attributes = client.attributes().getAttributes(lookup, includeSongCounts = true)
```

## Events

StreamerSonglist documents Centrifugo channel patterns for queue events. `streamersonglist4k` exposes typed helpers for those channels and keeps a raw channel escape hatch for undocumented additions.

```kotlin
import com.mbayou.streamersonglist4k.events.StreamerSonglistChannel
import com.mbayou.streamersonglist4k.events.StreamerSonglistEvent

val sessionFuture = client.events().connect(
    channels = listOf(StreamerSonglistChannel.queue(StreamerId(123))),
) { envelope ->
    when (val event = envelope.event) {
        is StreamerSonglistEvent.QueueAdd -> println(event.queue.song.title)
        is StreamerSonglistEvent.QueueRemove -> println("removed ${event.queueId.value}")
        StreamerSonglistEvent.QueueUpdate -> println("queue changed")
        else -> Unit
    }
}
```

## Design Boundary

This library talks to StreamerSonglist. It intentionally does not contain ai_licia-specific behavior such as raider grace windows, action execution, persistence, channel event emission, or Spring wiring.

Product workflows should compose this client from the application layer.
