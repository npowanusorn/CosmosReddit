package com.hamburghini.cosmos.data.model

import com.google.gson.annotations.SerializedName
import kotlin.random.Random

data class SubredditAbout(
    @SerializedName("data") val data: SubredditAboutData
)

data class SubredditAboutData(
    @SerializedName("display_name") val displayName: String,
    @SerializedName("display_name_prefixed") val displayNamePrefixed: String,
    @SerializedName("name") val name: String, // fullname (e.g., "t5_2qh0u")
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("public_description") val publicDescription: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("description_html") val descriptionHtml: String?,
    @SerializedName("subscribers") val subscribers: Int = 0,
    @SerializedName("accounts_active") val accountsActive: Int?,
    @SerializedName("icon_img") val iconImg: String?,
    @SerializedName("banner_img") val bannerImg: String?,
    @SerializedName("banner_background_image") val bannerBackgroundImage: String?,
    @SerializedName("header_img") val headerImg: String?,
    @SerializedName("community_icon") val communityIcon: String?,
    @SerializedName("over18") val over18: Boolean = false,
    @SerializedName("created_utc") val createdUtc: Long = 0L,
    @SerializedName("user_is_subscriber") val userIsSubscriber: Boolean?,
    @SerializedName("user_is_moderator") val userIsModerator: Boolean?,
    @SerializedName("user_is_banned") val userIsBanned: Boolean?,
    @SerializedName("user_is_muted") val userIsMuted: Boolean?,
    @SerializedName("subreddit_type") val subredditType: String?, // "public", "private", "restricted"
    @SerializedName("submission_type") val submissionType: String?, // "any", "link", "self"
    @SerializedName("allow_images") val allowImages: Boolean?,
    @SerializedName("allow_videos") val allowVideos: Boolean?,
    @SerializedName("primary_color") val primaryColor: String?,
    @SerializedName("key_color") val keyColor: String?,
    @SerializedName("active_user_count") val activeUserCount: Int?,
    @SerializedName("restrict_posting") val restrictPosting: Boolean?,
    @SerializedName("restrict_commenting") val restrictCommenting: Boolean?,
    @SerializedName("quarantine") val quarantine: Boolean?,
    @SerializedName("url") val url: String?
) {
    companion object {
        private val random = Random(Random.nextInt())
        val mock = SubredditAboutData(
            displayName = "AndroidDev",
            displayNamePrefixed = "r/AndroidDev",
            name = "t5_${random.nextInt(100000)}",
            id = random.nextInt(100000).toString(),
            title = "Android Developers Community",
            publicDescription = "A community for Android developers.",
            description = "Longer description about Android development.",
            descriptionHtml = "<p>Android development discussion</p>",
            subscribers = random.nextInt(1_000, 5_000_000),
            accountsActive = random.nextInt(10, 50_000),
            iconImg = null,
            bannerImg = null,
            bannerBackgroundImage = null,
            headerImg = null,
            communityIcon = null,
            over18 = false,
            createdUtc = System.currentTimeMillis() / 1000 - random.nextLong(
                1_000_000,
                100_000_000
            ),
            userIsSubscriber = random.nextBoolean(),
            userIsModerator = false,
            userIsBanned = false,
            userIsMuted = false,
            subredditType = "public",
            submissionType = "any",
            allowImages = true,
            allowVideos = true,
            primaryColor = "#3DDC84",
            keyColor = "#1E88E5",
            activeUserCount = random.nextInt(0, 20_000),
            restrictPosting = false,
            restrictCommenting = false,
            quarantine = false,
            url = "https://www.reddit.com/r/AndroidDev/"
        )
    }
}