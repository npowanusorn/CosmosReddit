package com.hamburghini.cosmos.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.hamburghini.cosmos.core.constants.Constants
import com.hamburghini.cosmos.model.Post
import com.hamburghini.cosmos.ui.screens.VideoPlayerScreen
import com.hamburghini.cosmos.ui.theme.CosmosTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VideoPlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val videoUrl = intent.getStringExtra(Constants.VIDEO_CLICKED_PARCELABLE)
        if (videoUrl == null) {
            Toast.makeText(this, "Error opening this video", Toast.LENGTH_SHORT).show()
            return finish()
        }

        setContent {
            CosmosTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(
                        modifier = Modifier.padding(innerPadding),
                        color = Color.Black
                    ) {
                        VideoPlayerScreen(
                            contentUrl = videoUrl
                        )
                    }
                }
            }
        }
    }
}