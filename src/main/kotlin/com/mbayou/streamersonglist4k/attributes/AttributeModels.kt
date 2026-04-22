package com.mbayou.streamersonglist4k.attributes

import com.fasterxml.jackson.annotation.JsonProperty
import com.mbayou.streamersonglist4k.api.AttributeId
import com.mbayou.streamersonglist4k.api.StreamerId

data class GetAttributesResponseBody(
    @JsonProperty("\$schema")
    val schema: String? = null,
    val items: List<AttributeSummary>?,
    val total: Long,
)

data class Attribute(
    @JsonProperty("\$schema")
    val schema: String? = null,
    val id: AttributeId,
    val name: String,
    val image: String?,
    val priority: Int?,
    val active: Boolean,
    val show: Boolean,
    val followerOnly: Boolean,
    val subscriberOnly: Boolean,
    val streamerId: StreamerId,
    val songOrdering: Int?,
    val minAmount: Int?,
    val subTier: String?,
    val showSelector: Boolean,
    val overrideNew: Boolean,
    val bypassRequestLimits: Boolean,
    val restrictions: AttributeRestrictions,
)

data class AttributeSummary(
    val id: AttributeId,
    val name: String,
    val image: String?,
    val priority: Int?,
    val active: Boolean,
    val show: Boolean,
    val followerOnly: Boolean,
    val subscriberOnly: Boolean,
    val streamerId: StreamerId,
    val songOrdering: Int?,
    val minAmount: Int?,
    val subTier: String?,
    val showSelector: Boolean,
    val overrideNew: Boolean,
    val bypassRequestLimits: Boolean,
    val restrictions: AttributeRestrictions,
    val activeSongs: Int,
    val totalSongs: Int,
)

data class AttributeRestrictions(
    val kick: KickRestrictions? = null,
    val twitch: TwitchRestrictions? = null,
    val youtube: YoutubeRestrictions? = null,
)

data class KickRestrictions(
    val subscriptions: Boolean? = null,
)

data class TwitchRestrictions(
    val follower: Boolean? = null,
    val subscriber: Boolean? = null,
)

data class YoutubeRestrictions(
    val member: Boolean? = null,
)

data class CreateAttributeRequest(
    val name: String,
    val active: Boolean,
    val show: Boolean,
    val showSelector: Boolean,
    val bypassRequestLimits: Boolean,
    val overrideNew: Boolean,
    val streamerId: StreamerId,
    val minAmount: Int? = null,
    val priority: Int? = null,
    val restrictions: AttributeRestrictions? = null,
    val songOrdering: Int? = null,
)

data class UpdateAttributeRequest(
    val name: String? = null,
    val active: Boolean? = null,
    val show: Boolean? = null,
    val showSelector: Boolean? = null,
    val overrideNew: Boolean? = null,
    val minAmount: Int? = null,
    val priority: Int? = null,
    val restrictions: AttributeRestrictions? = null,
    val songOrdering: Int? = null,
)
