package com.hamburghini.cosmos.core.util

import com.hamburghini.cosmos.data.model.SubredditAboutData

object SubredditUtils {
    fun getBannerUrl(subredditAboutData: SubredditAboutData): String? {
        if (!subredditAboutData.bannerImg.isNullOrBlank()) {
            return subredditAboutData.bannerImg.decodeHtml()
        } else if (!subredditAboutData.bannerBackgroundImage.isNullOrBlank()) {
            return subredditAboutData.bannerBackgroundImage.decodeHtml()
        }
        return null
    }
}
