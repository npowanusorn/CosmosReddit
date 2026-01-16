package com.hamburghini.cosmos.ui.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.hamburghini.cosmos.core.constants.Constants
import com.hamburghini.cosmos.core.util.Logger
import com.hamburghini.cosmos.data.model.Post
import com.hamburghini.cosmos.ui.components.PostMenuBottomSheet
import com.hamburghini.cosmos.ui.components.RedditTopAppBar
import com.hamburghini.cosmos.ui.screens.postdetail.PostDetailScreen
import com.hamburghini.cosmos.ui.theme.CosmosTheme
import com.hamburghini.cosmos.ui.screens.postdetail.PostDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
            var showPostMenu by remember { mutableStateOf(false) }
            val coroutineScope = rememberCoroutineScope()
            val context = LocalContext.current
            val snackbarHostState = remember { SnackbarHostState() }

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
                                        showPostMenu = true
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

                    if (showPostMenu) {
                        PostMenuBottomSheet(
                            post = post,
                            isLoggedIn = viewModel.isLoggedIn(),
                            onDismissRequest = {
                                showPostMenu = false
                            },
                            onSaveClick = {
                                // Toggle save state
                                val newSaveState = !post.saved
                                viewModel.savePost(post.name, newSaveState)

                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (newSaveState) "Post saved" else "Post unsaved"
                                    )
                                }
                            },
                            onHideClick = {
                                // TODO: Implement hide functionality
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (post.hidden == true) "TODO: Post unhidden" else "TODO: Post hidden"
                                    )
                                }
                            },
                            onReportClick = {
                                // TODO: Navigate to report screen
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Report functionality coming soon")
                                }
                            },
                            onShareClick = {
                                sharePost(context, post)
                            },
                            onCopyLinkClick = {
                                copyPostLink(context, post)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Link copied to clipboard")
                                }
                            },
                            onBlockUserClick = {
                                // TODO: Implement block user functionality
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Block user functionality coming soon")
                                }
                            },
                            onViewProfileClick = {
                                // TODO: Navigate to user profile
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Navigate to u/${post.author}")
                                }
                            },
                            onViewSubredditClick = {
                                // TODO: Navigate to subreddit
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Navigate to ${post.subreddit_name_prefixed}")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun sharePost(context: Context, post: Post) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, post.title)
        putExtra(Intent.EXTRA_TEXT, "https://reddit.com${post.permalink}")
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share post via"))
}

private fun copyPostLink(context: Context, post: Post) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Reddit post link", "https://reddit.com${post.permalink}")
    clipboard.setPrimaryClip(clip)
}