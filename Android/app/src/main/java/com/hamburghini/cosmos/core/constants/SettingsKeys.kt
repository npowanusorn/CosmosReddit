package com.hamburghini.cosmos.core.constants

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object SettingsKeys {
    val THEME = stringPreferencesKey("theme")
    val COMPACT_MODE = booleanPreferencesKey("compact_mode")
    val SHOW_NSFW = booleanPreferencesKey("show_nsfw")
    val BLUR_NSFW = booleanPreferencesKey("blur_nsfw")
    val AUTOPLAY_VIDEO = stringPreferencesKey("autoplay_video")
    val DEFAULT_SORT = stringPreferencesKey("default_sort")
}

enum class AppTheme { SYSTEM, LIGHT, DARK }
enum class AutoplayVideo { ALWAYS, WIFI_ONLY, NEVER }
enum class PostSort { HOT, NEW, TOP, RISING }
