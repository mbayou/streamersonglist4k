# streamersonglist4k [![](https://jitpack.io/v/mbayou/streamersonglist4k.svg)](https://jitpack.io/#mbayou/streamersonglist4k)

Kotlin/JVM client for the new StreamerSonglist API.

`streamersonglist4k` is built and maintained by [NovaSquare Ltd](https://www.getailicia.com/about-us), the editor of
[ai_licia](https://getailicia.com), the ultimate co-host for online communities.

We built this client while integrating StreamerSonglist into ai_licia and made it open source for anyone who wants to
build Kotlin applications, bots, dashboards, overlays, workflow tools, or community automation on top of the new
StreamerSonglist API.

## Why Use It?

- Strongly typed Kotlin models for StreamerSonglist resources.
- OAuth and streamer-token authentication support.
- Queue, songs, attributes, play history, streamer, and user clients.
- Centrifugo event helpers for realtime queue updates.
- Java 21 / Kotlin JVM library with no framework dependency.
- Designed for backend services, desktop apps, bots, and automation tools.

## API Coverage

The current release targets the documented StreamerSonglist V2 API:

- https://dev.staging.streamersonglist.com/docs/overview
- https://api.staging.streamersonglist.com/openapi.json

Implemented in `0.2.1`:

- OAuth authorization URL, authorization-code exchange, client credentials, and refresh token helpers.
- Streamer access token and OAuth bearer token authentication.
- Streamers and authenticated-user endpoints.
- Queue read/create/update/delete, mark played, and request management.
- Songs read/search/create/update/delete/export/bulk-update helpers.
- Attributes read/create/update/delete helpers.
- Play history read/create/update/delete/export helpers.
- Typed event parsing for queue, play-history, and saved-queue events.

## Install

Published builds are intended to be consumed through JitPack:

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.mbayou:streamersonglist4k:0.2.1")
}
```

## Quick Start

```kotlin
import com.mbayou.streamersonglist4k.StreamerSonglistClient
import com.mbayou.streamersonglist4k.StreamerSonglistConfiguration
import com.mbayou.streamersonglist4k.api.StreamerId
import com.mbayou.streamersonglist4k.api.StreamerLookup
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

val queue = client.queue().getQueue(StreamerLookup.ById(StreamerId(123)))
println("Queue size: ${queue.total}")
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

## Contributing

Contributions are welcome. This project is intended to be useful beyond ai_licia, so issues and pull requests from
anyone building on StreamerSonglist are encouraged.

Before opening a PR:

1. Keep public APIs strongly typed. Avoid exposing `Map<String, Any?>` or raw strings when a typed model or enum is practical.
2. Keep framework-specific code out of the library. Consumers should be able to use it from Spring, Ktor, desktop apps, CLI tools, or plain JVM services.
3. Add or update focused tests for behavior that can regress.
4. Run:

```bash
gradle clean test --console=plain
```

5. Describe the StreamerSonglist endpoint or payload your change covers, and link the relevant documentation when possible.

## Development

Requirements:

- JDK 21
- Gradle 9 installed locally

Common commands:

```bash
gradle test --console=plain
gradle clean build --console=plain
```

## License

MIT. See [LICENSE](LICENSE).

## About ai_licia

[ai_licia](https://getailicia.com) is the ultimate co-host for online communities. It helps streamers create more
reactive, entertaining, and community-aware live experiences across chat, games, overlays, integrations, and automation.

`streamersonglist4k` is one of the open source building blocks NovaSquare maintains for the creator-tech ecosystem.
