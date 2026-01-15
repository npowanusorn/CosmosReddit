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
     * @param imageUrls List of image URLs to display
     * @param initialPage Starting page index
     */
    fun launchPhotoViewer(
        context: Context,
        imageUrls: List<String>,
        initialPage: Int = 0
    ) {
        if (imageUrls.isEmpty()) return

        val intent = Intent(context, PhotoViewerActivity::class.java).apply {
            putStringArrayListExtra(
                PhotoViewerActivity.EXTRA_IMAGE_URLS,
                ArrayList(imageUrls)
            )
            putExtra(PhotoViewerActivity.EXTRA_INITIAL_PAGE, initialPage)
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
    fun extractImageUrls(post: Post): List<String> {
        val imageUrls = mutableListOf<String>()

        Logger.i("extractImageUrls: ${post.title}")
        // Handle gallery posts
        if (post.is_gallery == true && post.gallery_data != null && post.media_metadata != null) {
            post.gallery_data.items.forEach { galleryItem ->
                post.media_metadata[galleryItem.mediaId]?.let { metadata ->
                    (metadata.mediaSource.url ?: metadata.mediaSource.gif)?.let { url ->
                        val imageUrl = url
                            .replace("&amp;", "&")
                            .replace("preview.redd.it", "i.redd.it")
                        imageUrls.add(imageUrl)
                    }
                }
            }
        }

        // Handle single image posts
        if (imageUrls.isEmpty() && post.post_hint == "image") {
            PostUtils.getImageUrl(post)?.let { imageUrl ->
                imageUrls.add(imageUrl)
            }
        }

        // Handle preview images as fallback
        if (imageUrls.isEmpty() && post.preview?.images?.isNotEmpty() == true) {
            post.preview.images.forEach { image ->
                image.source?.url?.let { url ->
                    val cleanUrl = url.replace("&amp;", "&")
                    if (cleanUrl !in imageUrls) {
                        imageUrls.add(cleanUrl)
                    }
                }
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
    fun hasViewableImages(post: Post): Boolean {
        return when {
            // Gallery posts
            post.is_gallery == true && post.gallery_data != null && post.media_metadata != null -> {
                post.gallery_data.items.isNotEmpty()
            }
            // Single image posts
            post.post_hint == "image" -> {
                PostUtils.getImageUrl(post) != null
            }
            // Preview images
            post.preview?.images?.isNotEmpty() == true -> true
            else -> false
        }
    }

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