package com.hamburghini.cosmos.model

import com.google.gson.annotations.SerializedName

data class Post(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String, // Full ID, e.g., "t3_xxxxxx"
    @SerializedName("title") val title: String,
    @SerializedName("author") val author: String,
    @SerializedName("subreddit") val subreddit: String,
    @SerializedName("subreddit_id") val subreddit_id: String,
    @SerializedName("subreddit_name_prefixed") val subreddit_name_prefixed: String,
    @SerializedName("score") var score: Int,
    @SerializedName("num_comments") val num_comments: Int,
    @SerializedName("permalink") val permalink: String,
    @SerializedName("url") val url: String, // URL to the content (image, external link, etc.)
    @SerializedName("thumbnail") val thumbnail: String?, // URL to post thumbnail
    @SerializedName("created_utc") val created_utc: Long, // Unix timestamp in UTC
    @SerializedName("selftext") val selftext: String?, // Raw markdown content of a text post
    @SerializedName("selftext_html") val selftext_html: String?, // HTML rendered markdown content
    @SerializedName("likes") var likes: Boolean?, // true if upvoted, false if downvoted, null if no vote. Make mutable.
    @SerializedName("saved") val saved: Boolean, // true if saved, false otherwise
    @SerializedName("preview") val preview: Preview?,
    @SerializedName("post_hint") val post_hint: String?, // "image", "link", "hosted:video", etc.
    @SerializedName("is_self") val is_self: Boolean, // Indicates if it's a self-text post
    @SerializedName("media") val media: Media?,
    @SerializedName("secure_media") val secure_media: Media?, // secure media object (preferred)
    @SerializedName("is_gallery") val is_gallery: Boolean?,
    @SerializedName("gallery_data") val gallery_data: GalleryData?,
    @SerializedName("media_metadata") val media_metadata: Map<String, MediaMetadataItem>?,
    @SerializedName("link_flair_text") val link_flair_text: String?, // Flair text like "OC", "Discussion", etc.
    @SerializedName("over_18") val over_18: Boolean, // NSFW flag
    @SerializedName("spoiler") val spoiler: Boolean?, // Spoiler flag
    @SerializedName("hidden") val hidden: Boolean?, // Hidden flag
    @SerializedName("pinned") val pinned: Boolean?, // Pinned post flag
    @SerializedName("stickied") val stickied: Boolean?, // Stickied post flag
    @SerializedName("locked") val locked: Boolean?, // Locked post flag
    @SerializedName("archived") val archived: Boolean?, // Archived post flag
    @SerializedName("contest_mode") val contest_mode: Boolean?, // Contest mode flag
    @SerializedName("quarantine") val quarantine: Boolean? // Quarantined post flag
)

data class Preview(
    @SerializedName("images") val images: List<Image>?,
    @SerializedName("enabled") val enabled: Boolean?
    // You might also find "reddit_video_preview" here for video posts,
    // but we'll keep it simple for now and focus on image previews.
)

data class Image(
    @SerializedName("source") val source: Source?,
    @SerializedName("resolutions") val resolutions: List<Source>?, // Resolutions also use the Source structure
    @SerializedName("id") val id: String?
)

data class Source(
    @SerializedName("url") val url: String,
    @SerializedName("width") val width: Int,
    @SerializedName("height") val height: Int
)

/**
 * Represents the 'media' or 'secure_media' object in a Reddit Post.
 * It can contain details for Reddit-hosted videos or external oEmbed content.
 */
data class Media(
    @SerializedName("reddit_video") val reddit_video: RedditVideo?, // Details for Reddit-hosted videos
    @SerializedName("oembed") val oembed: OEmbed?, // Details for oEmbed content (e.g., YouTube, Imgur)
    @SerializedName("type") val type: String? // The type of media, e.g., "youtube.com", "i.redd.it"
)

/**
 * Represents the 'reddit_video' object within the 'media' field.
 * Contains URLs and properties for Reddit's hosted videos/GIFs.
 */
data class RedditVideo(
    @SerializedName("fallback_url") val fallback_url: String?, // Direct URL to the video, suitable for playback
    @SerializedName("height") val height: Int?,
    @SerializedName("width") val width: Int?,
    @SerializedName("scrubber_media_url") val scrubber_media_url: String?,
    @SerializedName("dash_url") val dash_url: String?, // URL for DASH manifest
    @SerializedName("duration") val duration: Int?, // Video duration in seconds
    @SerializedName("hls_url") val hls_url: String?, // URL for HLS manifest
    @SerializedName("is_gif") val is_gif: Boolean?, // True if it's a GIF that's been converted to video
    @SerializedName("transcoding_status") val transcoding_status: String?
)

/**
 * Represents the 'oembed' object within the 'media' field.
 * Contains details for rich external content that supports oEmbed, like YouTube videos.
 */
data class OEmbed(
    @SerializedName("provider_url") val provider_url: String?, // URL of the content provider (e.g., "https://www.youtube.com/")
    @SerializedName("version") val version: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("type") val type: String?, // Type of the oEmbed content, e.g., "video", "photo"
    @SerializedName("thumbnail_width") val thumbnail_width: Int?,
    @SerializedName("height") val height: Int?,
    @SerializedName("width") val width: Int?,
    @SerializedName("html") val html: String?, // Embeddable HTML (e.g., an <iframe> for videos)
    @SerializedName("author_name") val author_name: String?,
    @SerializedName("provider_name") val provider_name: String?, // Name of the content provider (e.g., "YouTube")
    @SerializedName("thumbnail_url") val thumbnail_url: String?, // URL for a thumbnail image
    @SerializedName("thumbnail_height") val thumbnail_height: Int?,
    @SerializedName("author_url") val author_url: String?
)

data class GalleryItem(
    @SerializedName("media_id") val mediaId: String
)

data class GalleryData(
    val items: List<GalleryItem>
)

data class MediaMetadataItem(
    @SerializedName("status") val status: String,
    @SerializedName("s") val mediaSource: MediaSource,
    @SerializedName("p") val downscaledSource: List<MediaSource>
)

data class MediaSource(
    @SerializedName("u") val url: String,
    @SerializedName("x") val width: Int,
    @SerializedName("y") val height: Int
)