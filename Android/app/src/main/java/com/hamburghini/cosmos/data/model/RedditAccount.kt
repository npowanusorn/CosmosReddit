package com.hamburghini.cosmos.data.model

import com.google.gson.annotations.SerializedName

data class RedditAccount(
    @SerializedName("username") val username: String,
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("icon_img") val iconImg: String? = null,
    @SerializedName("is_active") var isActive: Boolean = false,
    @SerializedName("added_timestamp") val addedTimestamp: Long = System.currentTimeMillis()
)