package com.hamburghini.cosmos.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = RedditOrange,
    secondary = RedditBlue,
    tertiary = RedditGold,
    background = RedditDarkBackground,
    surface = RedditDarkSurface,
    onBackground = RedditDarkOnBackground,
    onSurface = RedditDarkOnSurface
)

private val LightColorScheme = lightColorScheme(
    primary = RedditOrange,
    secondary = RedditBlue,
    tertiary = RedditGold,
    background = RedditLightBackground,
    surface = RedditLightSurface,
    onBackground = RedditLightOnBackground,
    onSurface = RedditLightOnSurface
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CosmosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}