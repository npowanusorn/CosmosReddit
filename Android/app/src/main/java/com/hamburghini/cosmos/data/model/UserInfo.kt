package com.hamburghini.cosmos.data.model

import com.google.gson.annotations.SerializedName

data class UserInfo(
    @SerializedName("name") val name: String,
    @SerializedName("id") val id: String,
    @SerializedName("icon_img") val iconImg: String?,
    @SerializedName("link_karma") val linkKarma: Int,
    @SerializedName("comment_karma") val commentKarma: Int,
    @SerializedName("total_karma") val totalKarma: Int,
    @SerializedName("created_utc") val createdUtc: Long
)