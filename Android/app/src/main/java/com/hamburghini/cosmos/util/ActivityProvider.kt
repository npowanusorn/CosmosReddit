package com.hamburghini.cosmos.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Find the Activity from a Compose Context
 */
fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) {
            return context
        }
        context = context.baseContext
    }
    return null
}

/**
 * Composable to get the current Activity
 */
@Composable
fun getActivity(): Activity? {
    val context = LocalContext.current
    return context.findActivity()
}