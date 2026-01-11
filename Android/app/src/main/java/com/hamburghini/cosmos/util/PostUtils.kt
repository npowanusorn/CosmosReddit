package com.hamburghini.cosmos.util

import com.hamburghini.cosmos.model.Post
import java.util.Locale
import java.util.concurrent.TimeUnit

object PostUtils {

    fun formatScore(score: Int): String {
        return when {
            score >= 1000000 -> String.format(Locale.getDefault(), "%.1fM", score / 1000000.0)
            score >= 1000 -> String.format(Locale.getDefault(), "%.1fk", score / 1000.0)
            else -> score.toString()
        }
    }

    fun formatCommentCount(count: Int): String {
        return when {
            count >= 1000000 -> String.format(Locale.getDefault(), "%.1fM", count / 1000000.0)
            count >= 1000 -> String.format(Locale.getDefault(), "%.1fk", count / 1000.0)
            else -> count.toString()
        }
    }

    fun formatTimeAgo(createdUtc: Long): String {
        val currentTime = System.currentTimeMillis()
        val createdTime = createdUtc * 1000
        val diff = currentTime - createdTime

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "just now"
            diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)}m ago"
            diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)}h ago"
            diff < TimeUnit.DAYS.toMillis(30) -> "${TimeUnit.MILLISECONDS.toDays(diff)}d ago"
            diff < TimeUnit.DAYS.toMillis(365) -> {
                val months = TimeUnit.MILLISECONDS.toDays(diff) / 30
                "${months}mo ago"
            }
            else -> {
                val years = TimeUnit.MILLISECONDS.toDays(diff) / 365
                "${years}y ago"
            }
        }
    }

    fun getPostType(post: Post): PostType {
        return when {
            post.is_gallery == true -> PostType.GALLERY
            post.post_hint == "image" -> PostType.IMAGE
            post.post_hint == "hosted:video" || post.media?.reddit_video != null -> PostType.VIDEO
            post.post_hint == "link" -> PostType.LINK
            post.is_self -> PostType.TEXT
            else -> PostType.UNKNOWN
        }
    }

    fun getImageUrl(post: Post): String? {
        // Try to get the best quality image URL
        return post.preview?.images?.firstOrNull()?.source?.url?.replace("&amp;", "&")
    }

    fun getThumbnailUrl(post: Post): String? {
        return when {
            post.thumbnail?.startsWith("http") == true -> post.thumbnail
            post.preview?.images?.firstOrNull()?.resolutions?.lastOrNull()?.url != null -> {
                post.preview.images.first().resolutions?.last()?.url?.replace("&amp;", "&")
            }
            else -> null
        }
    }

    fun getVideoUrl(post: Post): String? {
        return post.secure_media?.reddit_video?.fallback_url
            ?: post.media?.reddit_video?.fallback_url
    }

    fun cleanSelfText(selftext: String?): String? {
        return selftext?.takeIf { it.isNotBlank() && it != "[removed]" && it != "[deleted]" }
    }

    fun getFlairText(post: Post): String? {
        return post.link_flair_text?.takeIf { it.isNotBlank() }
    }
}

enum class PostType {
    TEXT, IMAGE, VIDEO, LINK, GALLERY, UNKNOWN
}