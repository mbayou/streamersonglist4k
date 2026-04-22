package com.mbayou.streamersonglist4k.api

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

interface IntIdentifier {
    val value: Int
}

interface ApiEnum {
    @get:JsonValue
    val apiValue: String
}

@JvmInline
value class StreamerId @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(
    @get:JsonValue override val value: Int,
) : IntIdentifier

@JvmInline
value class UserId @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(
    @get:JsonValue override val value: Int,
) : IntIdentifier

@JvmInline
value class SongId @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(
    @get:JsonValue override val value: Int,
) : IntIdentifier

@JvmInline
value class QueueId @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(
    @get:JsonValue override val value: Int,
) : IntIdentifier

@JvmInline
value class RequestId @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(
    @get:JsonValue override val value: Int,
) : IntIdentifier

@JvmInline
value class AttributeId @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(
    @get:JsonValue override val value: Int,
) : IntIdentifier

@JvmInline
value class PlayHistoryId @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(
    @get:JsonValue override val value: Int,
) : IntIdentifier

@JvmInline
value class SavedQueueId @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(
    @get:JsonValue override val value: Int,
) : IntIdentifier

enum class StreamerPlatform(override val apiValue: String) : ApiEnum {
    KICK("kick"),
    NONE("none"),
    TWITCH("twitch"),
    YOUTUBE("youtube"),
}

enum class SortDirection(override val apiValue: String) : ApiEnum {
    ASC("asc"),
    DESC("desc"),
}
