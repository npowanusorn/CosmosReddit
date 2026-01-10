package com.hamburghini.cosmos.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.ui.graphics.vector.ImageVector
import com.hamburghini.cosmos.R

enum class BottomNavDestination(
    val route: String,
    @StringRes val titleRes: Int,
    val icon: ImageVector,
    @DrawableRes val iconRes: Int? = null
) {
    HOME(
        route = "home",
        titleRes = R.string.tab_home,
        icon = Icons.Default.Home
    ),
    SUBREDDITS_LIST(
        route = "subreddits_list",
        titleRes = R.string.tab_subreddit_list,
        icon = Icons.Default.People
    ),
    CHAT(
        route = "chat",
        titleRes = R.string.tab_chat,
        icon = Icons.AutoMirrored.Filled.Chat
    ),
    PROFILE(
        route = "profile",
        titleRes = R.string.tab_profile,
        icon = Icons.Default.AccountCircle
    )
}