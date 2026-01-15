package com.hamburghini.cosmos.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.hamburghini.cosmos.core.constants.AppTheme
import com.hamburghini.cosmos.core.constants.AutoplayVideo
import com.hamburghini.cosmos.core.constants.PostSort
import com.hamburghini.cosmos.core.constants.SettingsKeys
import com.hamburghini.cosmos.core.util.settingsDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val dataStore = context.settingsDataStore

    val themeFlow: Flow<AppTheme> =
        dataStore.data.map {
            AppTheme.valueOf(it[SettingsKeys.THEME] ?: AppTheme.SYSTEM.name)
        }

    val compactModeFlow: Flow<Boolean> =
        dataStore.data.map { it[SettingsKeys.COMPACT_MODE] ?: false }

    val showNsfwFlow: Flow<Boolean> =
        dataStore.data.map { it[SettingsKeys.SHOW_NSFW] ?: false }

    val blurNsfwFlow: Flow<Boolean> =
        dataStore.data.map { it[SettingsKeys.BLUR_NSFW] ?: true }

    val autoplayVideoFlow: Flow<AutoplayVideo> =
        dataStore.data.map {
            AutoplayVideo.valueOf(
                it[SettingsKeys.AUTOPLAY_VIDEO] ?: AutoplayVideo.WIFI_ONLY.name
            )
        }

    val defaultSortFlow: Flow<PostSort> =
        dataStore.data.map {
            PostSort.valueOf(it[SettingsKeys.DEFAULT_SORT] ?: PostSort.HOT.name)
        }

    suspend fun setTheme(theme: AppTheme) {
        dataStore.edit { it[SettingsKeys.THEME] = theme.name }
    }

    suspend fun setCompactMode(enabled: Boolean) {
        dataStore.edit { it[SettingsKeys.COMPACT_MODE] = enabled }
    }

    suspend fun setShowNsfw(enabled: Boolean) {
        dataStore.edit { it[SettingsKeys.SHOW_NSFW] = enabled }
    }

    suspend fun setBlurNsfw(enabled: Boolean) {
        dataStore.edit { it[SettingsKeys.BLUR_NSFW] = enabled }
    }

    suspend fun setAutoplayVideo(value: AutoplayVideo) {
        dataStore.edit { it[SettingsKeys.AUTOPLAY_VIDEO] = value.name }
    }

    suspend fun setDefaultSort(sort: PostSort) {
        dataStore.edit { it[SettingsKeys.DEFAULT_SORT] = sort.name }
    }
}
