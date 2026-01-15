package com.hamburghini.cosmos.data.model

import com.google.gson.annotations.SerializedName

data class SubredditDetail(
    @SerializedName("display_name") val displayName: String?, // e.g., "aww"
    @SerializedName("icon_img") val icon_img: String?, // The primary icon URL for the subreddit
    @SerializedName("community_icon") val community_icon: String?, // Another potential icon URL (often for default subreddits)
    @SerializedName("primary_color") val primary_color: String?, // The primary color of the subreddit's theme
    @SerializedName("key_color") val key_color: String? // Another theme color
    // You can add more fields here if needed, like "subscribers", "public_description", etc.
)
