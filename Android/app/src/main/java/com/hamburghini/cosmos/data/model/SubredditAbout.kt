package com.hamburghini.cosmos.data.model

import com.google.gson.annotations.SerializedName
import com.hamburghini.cosmos.core.util.Logger
import java.net.URI
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
            description = "#**Welcome to r/Law!**#\\nThis is a place for lawyers and non-lawyers to discuss the legal profession and new and interesting legal developments from around the world.\\n----------------------------\\n#**Please follow the Rules!**#\\n/r/law is moderated more heavily than most other subs on reddit. Please consult the [Rules](https://www.reddit.com/r/law/about/rules/) before posting or commenting. \\n\\n/r/law is committed to civil and substantive discourse of relevant legal issues. Moderators are empowered to remove any post detracting from said discourse. All moderation decisions are final.\\n\\n-----------------------------\\n**Related Subreddits**\\n\\n * [r/lawschool](http://www.reddit.com/r/lawschool)\\n * [r/LSAT](http://www.reddit.com/r/LSAT)\\n * [r/SCOTUS](http://www.reddit.com/r/scotus)",
            descriptionHtml = "&lt;!-- SC_OFF --&gt;&lt;div class=\"md\"&gt;&lt;h1&gt;&lt;strong&gt;Welcome to &lt;a href=\"/r/Law\"&gt;r/Law&lt;/a&gt;!&lt;/strong&gt;&lt;/h1&gt;\\n\\n&lt;h2&gt;This is a place for lawyers and non-lawyers to discuss the legal profession and new and interesting legal developments from around the world.&lt;/h2&gt;\\n\\n&lt;h1&gt;&lt;strong&gt;Please follow the Rules!&lt;/strong&gt;&lt;/h1&gt;\\n\\n&lt;p&gt;&lt;a href=\"/r/law\"&gt;/r/law&lt;/a&gt; is moderated more heavily than most other subs on reddit. Please consult the &lt;a href=\"https://www.reddit.com/r/law/about/rules/\"&gt;Rules&lt;/a&gt; before posting or commenting. &lt;/p&gt;\\n\\n&lt;p&gt;&lt;a href=\"/r/law\"&gt;/r/law&lt;/a&gt; is committed to civil and substantive discourse of relevant legal issues. Moderators are empowered to remove any post detracting from said discourse. All moderation decisions are final.&lt;/p&gt;\\n\\n&lt;hr/&gt;\\n\\n&lt;p&gt;&lt;strong&gt;Related Subreddits&lt;/strong&gt;&lt;/p&gt;\\n\\n&lt;ul&gt;\\n&lt;li&gt;&lt;a href=\"http://www.reddit.com/r/lawschool\"&gt;r/lawschool&lt;/a&gt;&lt;/li&gt;\\n&lt;li&gt;&lt;a href=\"http://www.reddit.com/r/LSAT\"&gt;r/LSAT&lt;/a&gt;&lt;/li&gt;\\n&lt;li&gt;&lt;a href=\"http://www.reddit.com/r/scotus\"&gt;r/SCOTUS&lt;/a&gt;&lt;/li&gt;\\n&lt;/ul&gt;\\n&lt;/div&gt;&lt;!-- SC_ON --&gt;",
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

fun SubredditAboutData.iconUrl(): String? {
    Logger.i("subreddit: $displayName, iconImg: $iconImg, communityIcon: $communityIcon, headerImg: $headerImg")
    val fullUrl = when {
        !communityIcon.isNullOrBlank() -> communityIcon
        !iconImg.isNullOrBlank() -> iconImg
        !headerImg.isNullOrBlank() -> headerImg
        else -> null
    }

    if (fullUrl.isNullOrBlank()) return null
    val uri = URI(fullUrl)
    val cleanUrl = URI(uri.scheme, uri.authority, uri.path, null, null).toString()
    return cleanUrl
}