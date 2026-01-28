package com.hamburghini.cosmos.core.util

import android.content.Context
import android.content.Intent
import com.hamburghini.cosmos.data.model.Post
import com.hamburghini.cosmos.ui.activity.PhotoViewerActivity

object PhotoViewerUtils {

    /**
     * Launch photo viewer activity
     *
     * @param context Context to launch activity from
     * @param redditImage List of image URLs to display
     * @param initialPage Starting page index
     */
    fun launchPhotoViewer(
        context: Context,
        redditImage: List<RedditImage>,
        postName: String,
        initialPage: Int = 0
    ) {
        if (redditImage.isEmpty()) return

        val intent = Intent(context, PhotoViewerActivity::class.java).apply {
            putParcelableArrayListExtra(
                PhotoViewerActivity.EXTRA_IMAGE_URLS,
                ArrayList(redditImage)
            )
            putExtra(PhotoViewerActivity.EXTRA_INITIAL_PAGE, initialPage)
            putExtra(PhotoViewerActivity.EXTRA_POST_NAME, postName)
        }
        context.startActivity(intent)
    }

    /**
     * Extract all image URLs from a post
     * Handles single images, galleries, and preview images
     *
     * @param post The Reddit post
     * @return List of image URLs
     */
    fun extractImageUrls(post: Post): List<RedditImage> {
        val imageUrls = mutableListOf<RedditImage>()

        // Handle gallery posts
        if (
            post.is_gallery == true &&
            post.gallery_data != null &&
            post.media_metadata != null
        ) {
            post.gallery_data.items.forEach { galleryItem ->
                val metadata = post.media_metadata[galleryItem.mediaId] ?: return@forEach

                val source = metadata.mediaSource
                val previews = metadata.downscaledSource

                val fullUrl = source.url
                    ?.decodeHtml()
                    ?.replace("preview.redd.it", "i.redd.it")
                    ?: source.gif?.decodeHtml()
                    ?: return@forEach

                val placeholderUrl = previews
                    .minByOrNull { it.width }
                    ?.url
                    ?.decodeHtml()
                    ?.replace("preview.redd.it", "i.redd.it")
                    ?: fullUrl

                imageUrls.add(
                    RedditImage(
                        placeholderUrl = placeholderUrl,
                        fullUrl = fullUrl,
                        width = source.width,
                        height = source.height
                    )
                )
            }
        }

        // Handle single image posts
        if (imageUrls.isEmpty() && post.post_hint == "image") {
            PostUtils.getRedditImage(post)?.let { image ->
                imageUrls.add(image)
            }
        }

        return imageUrls
    }

    /**
     * Check if post has viewable images
     *
     * @param post The Reddit post
     * @return True if post has images that can be viewed
     */
//    fun hasViewableImages(post: Post): Boolean {
//        return when {
//            // Gallery posts
//            post.is_gallery == true && post.gallery_data != null && post.media_metadata != null -> {
//                post.gallery_data.items.isNotEmpty()
//            }
//            // Single image posts
//            post.post_hint == "image" -> {
//                PostUtils.getImageUrl(post) != null
//            }
//            // Preview images
//            post.preview?.images?.isNotEmpty() == true -> true
//            else -> false
//        }
//    }

    /**
     * Get the best quality image URL from post
     * Prioritizes original source over resolutions
     *
     * @param post The Reddit post
     * @return Best quality image URL or null
     */
    fun getBestQualityImageUrl(post: Post): String? {
        // Try preview source first
        post.preview?.images?.firstOrNull()?.source?.url?.let { url ->
            return url.replace("&amp;", "&")
        }

        // Try post URL if it's an image
        if (post.post_hint == "image" && post.url.contains("i.redd.it")) {
            return post.url
        }

        return null
    }
}