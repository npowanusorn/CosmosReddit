package com.hamburghini.cosmos.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.hamburghini.cosmos.core.constants.Constants
import com.hamburghini.cosmos.core.util.Logger
import com.hamburghini.cosmos.data.model.Post
import com.hamburghini.cosmos.ui.components.RedditTopAppBar
import com.hamburghini.cosmos.ui.screens.postdetail.PostDetailScreen
import com.hamburghini.cosmos.ui.theme.CosmosTheme
import com.hamburghini.cosmos.ui.screens.postdetail.PostDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PostDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val post = intent.getParcelableExtra(Constants.CLICKED_POST_PARCELABLE, Post::class.java)
        if (post == null) {
            Toast.makeText(this, "Error opening this post", Toast.LENGTH_SHORT).show()
            return finish()
        }
        setContent {
            val viewModel: PostDetailViewModel = hiltViewModel()

            LaunchedEffect(Unit) {
                viewModel.setPost(post)
            }

            CosmosTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        RedditTopAppBar(
                            title = "Post",
                            navigationIcon = {
                                IconButton(
                                    onClick = { finish() }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = null,
                                    )
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = {
                                        Logger.i("post detail more icon tapped")
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = null
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        )
                    }
                ) { innerPadding ->
                    PostDetailScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}