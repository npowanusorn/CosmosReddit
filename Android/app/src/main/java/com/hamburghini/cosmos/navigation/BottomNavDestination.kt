package com.hamburghini.cosmos.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.People
import androidx.compose.ui.graphics.vector.ImageVector
import com.hamburghini.cosmos.R

enum class BottomNavDestination(
    val route: String,
    @StringRes val titleRes: Int,
    @DrawableRes val filledIconRes: Int,
    @DrawableRes val outlineIconRes: Int
) {
    HOME(
        route = "home",
        titleRes = R.string.tab_home,
        filledIconRes = R.drawable.ic_home_filled,
        outlineIconRes = R.drawable.ic_home
    ),
    SUBREDDITS_LIST(
        route = "subreddits_list",
        titleRes = R.string.tab_subreddit_list,
        filledIconRes = R.drawable.ic_communities_filled,
        outlineIconRes = R.drawable.ic_communities,
    ),
    CHAT(
        route = "chat",
        titleRes = R.string.tab_chat,
        filledIconRes = R.drawable.ic_chat_filled,
        outlineIconRes = R.drawable.ic_chat
    ),
    PROFILE(
        route = "profile",
        titleRes = R.string.tab_profile,
        filledIconRes = R.drawable.ic_account_circle_filled,
        outlineIconRes = R.drawable.ic_account_circle
    )
}