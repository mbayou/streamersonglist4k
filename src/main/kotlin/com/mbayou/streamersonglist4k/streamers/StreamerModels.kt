package com.mbayou.streamersonglist4k.streamers

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.mbayou.streamersonglist4k.api.StreamerId
import com.mbayou.streamersonglist4k.api.UserId
import java.time.Instant

data class StreamerDetails(
    @JsonProperty("\$schema")
    val schema: String? = null,
    val id: StreamerId,
    val userId: UserId?,
    val createdAt: Instant,
    val platforms: StreamerPlatforms,
    val integrationSettings: JsonNode,
    val allowDuplicates: Boolean,
    val allowLiveLearns: Boolean,
    val alwaysShowRequestDialog: Boolean,
    val botActive: Boolean,
    val canAnonymousEnterName: Boolean,
    val canAnonymousRequest: Boolean,
    val canFollowerRequest: Boolean,
    val canSubscriberRequest: Boolean,
    val canSubscriberT2Request: Boolean,
    val canSubscriberT3Request: Boolean,
    val canUserRequest: Boolean,
    val concurrentRequests: Int,
    val concurrentRequestsPerAnonymous: Int,
    val concurrentRequestsPerFollower: Int,
    val concurrentRequestsPerSub: Int,
    val concurrentRequestsPerSubTier2: Int,
    val concurrentRequestsPerSubTier3: Int,
    val concurrentRequestsPerUser: Int,
    val customTheme: JsonNode? = null,
    val donationPage: String,
    val donationsIgnoreLimits: Boolean,
    val enableCrossPlatformRequests: Boolean,
    val landingPageContent: JsonNode? = null,
    val limitAnonymousRequests: Boolean,
    val limitFollowerRequests: Boolean,
    val limitSubscriberRequests: Boolean,
    val limitSubscriberT2Requests: Boolean,
    val limitSubscriberT3Requests: Boolean,
    val limitUserRequests: Boolean,
    val liveLearnsNoSongFound: Boolean,
    val maxRequests: Int,
    val minAmount: Double,
    val minLiveLearnAmount: Double,
    val minTipAmountForQueueOrdering: Double,
    val minutesBetweenRequests: Int,
    val newDays: Int,
    val permitConfig: JsonNode? = null,
    val queueMethod: String,
    val requestMode: String,
    val requestText: String,
    val requestsActive: Boolean,
    val requestsPerAnonymous: Int,
    val requestsPerFollower: Int,
    val requestsPerSub: Int,
    val requestsPerSubTier2: Int,
    val requestsPerSubTier3: Int,
    val requestsPerUser: Int,
    val sessionLength: Int,
    val showDonationLink: Boolean,
    val theme: String,
)

data class StreamerPlatforms(
    val kick: StreamerPlatformDetails? = null,
    val none: StreamerPlatformDetails? = null,
    val twitch: StreamerPlatformDetails? = null,
    val youtube: StreamerPlatformDetails? = null,
)

data class StreamerPlatformDetails(
    @JsonProperty("platformID")
    val platformId: String,
    val profileImageUrl: String? = null,
    val username: String,
)
