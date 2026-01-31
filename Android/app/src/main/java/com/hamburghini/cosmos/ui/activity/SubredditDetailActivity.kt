package com.hamburghini.cosmos.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.hamburghini.cosmos.core.util.Logger
import com.hamburghini.cosmos.ui.screens.subredditdetail.SubredditDetailScreen
import com.hamburghini.cosmos.ui.screens.subredditdetail.SubredditDetailViewModel
import com.hamburghini.cosmos.ui.theme.CosmosTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubredditDetailActivity : ComponentActivity() {

    companion object {
        const val SUBREDDIT_NAME = "SUBREDDIT_NAME"
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val subredditName = intent.getStringExtra(SUBREDDIT_NAME)
        if (subredditName == null) {
            Toast.makeText(this, "Error opening this subreddit", Toast.LENGTH_SHORT).show()
            return finish()
        }

        setContent {
            val viewModel: SubredditDetailViewModel = hiltViewModel()
            var scrollProgress by remember { mutableFloatStateOf(0f) }

            LaunchedEffect(Unit) {
                viewModel.setSubredditName(subredditName)
            }

            CosmosTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                // Show subreddit name when scrolled past header
                                if (scrollProgress > 0.7f) {
                                    Text(
                                        text = "r/$subredditName",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = if (scrollProgress > 0.5f) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            Color.White
                                        }
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = { Logger.d("sort click") }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Sort,
                                        contentDescription = null,
                                        tint = if (scrollProgress > 0.5f) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            Color.White
                                        }
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = lerp(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background,
                                    scrollProgress
                                )
                            )
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .consumeWindowInsets(innerPadding)
                    ) {
                        SubredditDetailScreen(
                            viewModel = viewModel,
                            onScrollProgressChanged = { progress ->
                                scrollProgress = progress
                            }
                        )
                    }
                }
            }
        }
    }
}