package com.mbayou.streamersonglist4k.streamers

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.mbayou.streamersonglist4k.api.StreamerId
import com.mbayou.streamersonglist4k.api.UserId
import java.time.Instant

// Contract: https://api.streamersonglist.com/openapi.json (2026-11-02).
data class StreamerDetails(
    @JsonProperty("\$schema")
    val schema: String? = null,
    val adminBypassRestrictions: Boolean,
    val adminSyncConfig: AdminSyncConfig,
    val allowDuplicates: Boolean,
    val allowLiveLearns: Boolean,
    val alwaysShowRequestDialog: Boolean,
    val canAnonymousEnterName: Boolean,
    val canAnonymousRequest: Boolean,
    val canUserRequest: Boolean,
    val concurrentRequests: Int,
    val concurrentRequestsPerAdmin: Int,
    val concurrentRequestsPerAnonymous: Int,
    val concurrentRequestsPerUser: Int,
    val customTheme: CustomThemeColors,
    val donationPage: String,
    val donationsIgnoreLimits: Boolean,
    val embedOrigins: String,
    val enableCrossPlatformRequests: Boolean,
    val eventConfig: Map<String, EventConfigEntry>,
    val favorite: Boolean,
    val featureConfig: FeatureConfig,
    val fixedTokenPrice: Boolean,
    val homePageConfig: JsonNode?,
    val id: StreamerId,
    val integrationSettings: IntegrationSettings,
    val landingPageContent: JsonNode?,
    val learnListConfig: LearnListConfig,
    val limitAdminRequests: Boolean,
    val limitAnonymousRequests: Boolean,
    val liveLearnsNoSongFound: Boolean,
    val maxRequests: Int,
    val minAmount: Double,
    val minLiveLearnAmount: Double,
    val minTipAmountForQueueOrdering: Double,
    val minTokensForBump: Double,
    val minTokensToIgnoreLimits: Double,
    val minutesBetweenRequests: Int,
    val newAttribute: StreamerNewAttribute? = null,
    val newDays: Int,
    val permitConfig: PermitConfig,
    val platforms: StreamerPlatforms,
    val promoteQueueToPlaying: Boolean,
    val queueOrderMode: String,
    val queuePageConfig: QueuePageConfig,
    val requestMode: String,
    val requestText: String,
    val requestsActive: Boolean,
    val requestsPerAdmin: Int,
    val requestsPerAnonymous: Int,
    val requestsPerUser: Int,
    val sessionLength: Int,
    val showDonationLink: Boolean,
    val songListConfig: SongListConfig,
    val syncPlatformBans: Boolean,
    val theme: String,
    val tokenConfig: TokenConfig,
    val trackSongDuration: Boolean,
    val userId: UserId?,
)

data class AdminSyncConfig(
    val twitch: AdminSyncPlatformConfig? = null,
)

data class CustomThemeColors(
    val accent: String,
    val background: String? = null,
    val dark: CustomThemeDarkColors? = null,
    val foreground: String? = null,
    val primary: String,
)

data class EventConfigEntry(
    val chat: EventChatSinkConfig? = null,
)

data class FeatureConfig(
    val collabSessions: Boolean? = null,
    val duration: Boolean? = null,
    val globalSongSearch: Boolean? = null,
    val premiumContent: Boolean? = null,
    val savedRequests: Boolean? = null,
    val songsToLearn: Boolean? = null,
    val tokens: Boolean? = null,
)

data class IntegrationSettings(
    val kick: KickIntegrationSettings? = null,
    val streamelements: StreamelementsSettings? = null,
    val streamlabs: StreamlabsSettings? = null,
    val twitch: TwitchStreamerSettings? = null,
    val youtube: YoutubeStreamerSettings? = null,
)

data class LearnListConfig(
    @JsonProperty("\$schema")
    val schema: String? = null,
    val addQuotaPerUser: Long? = null,
    val maxActiveSongsPerUser: Long? = null,
    val requireApproval: Boolean? = null,
    val rewardMap: Map<String, LearnListReward>? = null,
    val tokensPerVote: Long? = null,
    val voteWeights: LearnListVoteWeights,
)

data class StreamerNewAttribute(
    val id: Int,
    val image: String?,
    val name: String,
    val show: Boolean,
    val showSelector: Boolean,
)

data class PermitConfig(
    val bypasses: List<String>?,
    val default_duration_seconds: Long,
    val no_expiry: Boolean,
)

data class StreamerPlatforms(
    val kick: StreamerPlatformDetails? = null,
    val none: StreamerPlatformDetails? = null,
    val twitch: StreamerPlatformDetails? = null,
    val youtube: StreamerPlatformDetails? = null,
)

data class QueuePageConfig(
    val public: QueuePageLayout? = null,
    val streamer: QueuePageLayout? = null,
)

data class SongListConfig(
    val artistSortIgnorePrefixes: List<String>? = null,
    val titleSortIgnorePrefixes: List<String>? = null,
)

data class TokenConfig(
    val chatSpendKeywords: List<String>? = null,
    val kickSub: Long? = null,
    val tokenValiditySeconds: Long? = null,
    val tokensPerBit: Double? = null,
    val tokensPerKick: Double? = null,
    val twitchSubTier1: Long? = null,
    val twitchSubTier2: Long? = null,
    val twitchSubTier3: Long? = null,
)

data class AdminSyncPlatformConfig(
    val defaultPermissions: AdminPermissions? = null,
    val enabled: Boolean,
)

data class CustomThemeDarkColors(
    val accent: String? = null,
    val background: String? = null,
    val foreground: String? = null,
    val primary: String? = null,
)

data class EventChatSinkConfig(
    val enabled: Boolean? = null,
    val platforms: Map<String, Boolean>? = null,
    val template: String? = null,
)

data class KickIntegrationSettings(
    val botConnected: Boolean,
    val follower: RequestTypeSettings,
    val subscriber: RequestTypeSettings,
    val user: RequestTypeSettings,
)

data class StreamelementsSettings(
    val sendChatMessage: Boolean,
)

data class StreamlabsSettings(
    val sendChatMessage: Boolean,
)

data class TwitchStreamerSettings(
    val botConnected: Boolean,
    val follower: RequestTypeSettings,
    val subscriberT1: RequestTypeSettings,
    val subscriberT2: RequestTypeSettings,
    val subscriberT3: RequestTypeSettings,
    val user: RequestTypeSettings,
    val vip: RequestTypeSettings,
)

data class YoutubeStreamerSettings(
    val botConnected: Boolean,
    val member: RequestTypeSettings,
    val membershipLevels: Map<String, RequestTypeSettings>,
    val subscriber: RequestTypeSettings,
    val user: RequestTypeSettings,
)

data class LearnListReward(
    val action: String,
    val votes: Long? = null,
)

data class LearnListVoteWeights(
    val default: Long? = null,
    val follower: Long? = null,
    val kickSubscriber: Long? = null,
    val twitchTier1: Long? = null,
    val twitchTier2: Long? = null,
    val twitchTier3: Long? = null,
    val youtubeMember: Long? = null,
)

data class StreamerPlatformDetails(
    val displayName: String,
    @JsonProperty("platformID")
    val platformId: String,
    val profileImageUrl: String? = null,
    val username: String,
)

data class QueuePageLayout(
    val columns: Map<String, String>? = null,
    val enabled: Boolean? = null,
    val nowPlaying: String? = null,
    val rows: List<List<QueueSummaryColumn>?>? = null,
    val summaryFirst: Boolean? = null,
)

data class AdminPermissions(
    val attribute: String? = null,
    val command: String? = null,
    val history: String? = null,
    val overlay: String? = null,
    val premiumContent: String? = null,
    val queue: String? = null,
    val settings: String? = null,
    val song: String? = null,
)

data class RequestTypeSettings(
    val concurrent: Long,
    val enabled: Boolean,
    val limit: Boolean,
    val requests: Long,
)

data class QueueSummaryColumn(
    val id: String,
    val tiles: List<QueueSummaryTile>?,
    val width: Long,
)

data class QueueSummaryTile(
    val id: String,
    val metric: String,
    val mode: String,
)
