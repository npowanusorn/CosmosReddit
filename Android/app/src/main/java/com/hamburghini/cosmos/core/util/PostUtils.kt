package com.hamburghini.cosmos.core.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import com.hamburghini.cosmos.data.model.Post
import kotlinx.parcelize.Parcelize
import java.util.Locale
import java.util.concurrent.TimeUnit

@Parcelize
data class RedditImage(
    val placeholderUrl: String,
    val fullUrl: String,
    val width: Int,
    val height: Int
) : Parcelable

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
            // 1. Galleries (Multiple images)
            post.is_gallery == true -> PostType.GALLERY

            // 2. Video (Internal and External)
            // rich:video covers YouTube, Vimeo, etc.
            post.post_hint == "hosted:video" ||
                    post.post_hint == "rich:video" ||
                    post.media?.reddit_video != null -> PostType.VIDEO

            // 3. Images
            // Sometimes hint is missing, so we check the URL extension as a fallback
            post.post_hint == "image" || post.url.contains(Regex("\\.(jpg|jpeg|png|gif)$", RegexOption.IGNORE_CASE)) -> PostType.IMAGE

            // 4. Polls (Reddit-specific)
//            post.poll_data != null -> PostType.POLL

            // 5. Self-posts (Text)
            // Check is_self or if it's a "self" hint
            post.is_self || post.post_hint == "self" -> PostType.TEXT

            // 6. Links
            post.post_hint == "link" || post.url.isNotEmpty() -> PostType.LINK

            else -> PostType.UNKNOWN
        }
    }

    fun getRedditImage(post: Post): RedditImage? {
        val image = post.preview?.images?.firstOrNull() ?: return null

        val source = image.source ?: return null
        val resolutions = image.resolutions ?: return null

        val fullUrl = source.url.decodeHtml()
        val placeholder = resolutions
            .minByOrNull { it.width }
            ?.url
            ?.decodeHtml()
            ?: fullUrl

        return RedditImage(
            placeholderUrl = placeholder,
            fullUrl = fullUrl,
            width = source.width,
            height = source.height
        )
    }

    fun getThumbnailUrl(post: Post): Pair<String, Float>? {
        val source = post.preview?.images?.firstOrNull()?.source ?: return null
        val url = source.url.replace("&amp;", "&")
        val width = source.width
        val height = source.height
        val aspectRatio = width.toFloat() / height.toFloat()
        Logger.i("url: $url")
        return Pair(url, aspectRatio)
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

    fun sharePost(context: Context, post: Post) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, post.title)
            putExtra(Intent.EXTRA_TEXT, "https://reddit.com${post.permalink}")
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share post via"))
    }

    fun copyPostLink(context: Context, post: Post) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Reddit post link", "https://reddit.com${post.permalink}")
        clipboard.setPrimaryClip(clip)
    }
}

enum class PostType {
    TEXT, IMAGE, VIDEO, LINK, GALLERY, UNKNOWN
}