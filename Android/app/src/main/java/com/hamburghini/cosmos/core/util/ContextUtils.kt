package com.hamburghini.cosmos.core.util

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.settingsDataStore by preferencesDataStore(
    name = "settings"
)
