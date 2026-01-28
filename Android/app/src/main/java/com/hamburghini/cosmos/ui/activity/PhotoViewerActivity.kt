package com.hamburghini.cosmos.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.hamburghini.cosmos.core.util.RedditImage
import com.hamburghini.cosmos.ui.screens.photoviewer.PhotoViewerScreen
import com.hamburghini.cosmos.ui.theme.CosmosTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PhotoViewerActivity : ComponentActivity() {

    companion object {
        const val EXTRA_IMAGE_URLS = "EXTRA_IMAGE_URLS"
        const val EXTRA_INITIAL_PAGE = "EXTRA_INITIAL_PAGE"
        const val EXTRA_POST_NAME = "EXTRA_POST_NAME"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Get image URLs from intent
        val redditImages = intent.getParcelableArrayListExtra(EXTRA_IMAGE_URLS, RedditImage::class.java) ?: arrayListOf()
        val initialPage = intent.getIntExtra(EXTRA_INITIAL_PAGE, 0)
        val postName = intent.getStringExtra(EXTRA_POST_NAME)

        // If no images provided, finish activity
        if (redditImages.isEmpty() && postName.isNullOrBlank()) {
            finish()
            return
        }

        setContent {
            CosmosTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PhotoViewerScreen(
                        redditImages = redditImages,
                        postId = postName!!,
                        initialPage = initialPage,
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }
}