package com.hamburghini.cosmos.data.model

import com.google.gson.annotations.SerializedName

data class Award(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("coin_price") val coinPrice: Int,
    @SerializedName("icon_url") val iconUrl: String,
    @SerializedName("icon_width") val iconWidth: Int,
    @SerializedName("icon_height") val iconHeight: Int,
    @SerializedName("days_of_premium") val daysOfPremium: Int?,
    @SerializedName("coin_reward") val coinReward: Int?,
    @SerializedName("subreddit_coin_reward") val subredditCoinReward: Int?,
    @SerializedName("count") val count: Int?, // Number available to give
    @SerializedName("award_type") val awardType: String // "global", "community"
)
