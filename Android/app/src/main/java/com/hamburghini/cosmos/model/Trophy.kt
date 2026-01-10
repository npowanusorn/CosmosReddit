package com.hamburghini.cosmos.model

import com.google.gson.annotations.SerializedName

data class Trophy(
    @SerializedName("data") val data: TrophyData
)

data class TrophyData(
    @SerializedName("trophies") val trophies: List<TrophyItem>
)

data class TrophyItem(
    @SerializedName("data") val data: TrophyInfo
)

data class TrophyInfo(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("icon_70") val icon70: String?,
    @SerializedName("icon_40") val icon40: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("award_id") val awardId: String?,
    @SerializedName("granted_at") val grantedAt: Long?
)