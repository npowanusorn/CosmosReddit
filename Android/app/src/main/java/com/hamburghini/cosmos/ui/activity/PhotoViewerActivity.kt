package com.hamburghini.cosmos.ui.activity

import android.os.Bundle
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.hamburghini.cosmos.ui.screens.PhotoViewerScreen
import com.hamburghini.cosmos.ui.theme.CosmosTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity for displaying full-screen photo viewer with zoom and swipe capabilities
 *
 * Expects the following extras:
 * - IMAGE_URLS: ArrayList<String> - List of image URLs to display
 * - INITIAL_PAGE: Int - Starting page index (default: 0)
 */
@AndroidEntryPoint
class PhotoViewerActivity : ComponentActivity() {

    companion object {
        const val EXTRA_IMAGE_URLS = "extra_image_urls"
        const val EXTRA_INITIAL_PAGE = "extra_initial_page"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Get image URLs from intent
        val imageUrls = intent.getStringArrayListExtra(EXTRA_IMAGE_URLS) ?: emptyList()
        val initialPage = intent.getIntExtra(EXTRA_INITIAL_PAGE, 0)

        // If no images provided, finish activity
        if (imageUrls.isEmpty()) {
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
                        imageUrls = imageUrls,
                        initialPage = initialPage,
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }
}