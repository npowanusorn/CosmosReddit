package com.hamburghini.cosmos.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.hamburghini.cosmos.ui.screens.subredditdetail.SubredditDetailViewModel
import com.hamburghini.cosmos.ui.theme.CosmosTheme

class SubredditDetailActivity : ComponentActivity() {

    companion object {
        const val SUBREDDIT_NAME = "SUBREDDIT_NAME"
    }

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

            LaunchedEffect(Unit) {
                viewModel.setSubredditName(subredditName)
            }

            CosmosTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CosmosTheme {
        Greeting("Android")
    }
}