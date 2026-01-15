package com.hamburghini.cosmos.data.manager

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.hamburghini.cosmos.model.SubredditAboutData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionCacheManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "SubscriptionCacheManager"
        private const val CACHE_DIR = "subscription_cache"
    }

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    /**
     * Get cache file for specific user
     */
    private fun getCacheFile(username: String): File {
        val cacheDir = File(context.filesDir, CACHE_DIR)
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        // Sanitize username to create valid filename
        val sanitizedUsername = username.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        return File(cacheDir, "subscriptions_$sanitizedUsername.json")
    }

    /**
     * Save subscriptions to cache
     */
    suspend fun saveSubscriptions(
        username: String,
        subreddits: List<SubredditAboutData>
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val cacheFile = getCacheFile(username)

            FileWriter(cacheFile).use { writer ->
                gson.toJson(subreddits, writer)
            }

            Log.d(TAG, "Saved ${subreddits.size} subscriptions to cache for user: $username")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save subscriptions cache for user: $username", e)
            false
        }
    }

    /**
     * Load subscriptions from cache
     * Returns null if cache doesn't exist, is invalid, or corrupted
     */
    suspend fun loadSubscriptions(username: String): List<SubredditAboutData>? = withContext(Dispatchers.IO) {
        try {
            val cacheFile = getCacheFile(username)

            if (!cacheFile.exists()) {
                Log.d(TAG, "No cache file found for user: $username")
                return@withContext null
            }

            FileReader(cacheFile).use { reader ->
                val type = object : TypeToken<List<SubredditAboutData>>() {}.type
                val subredditsList: List<SubredditAboutData> = gson.fromJson(reader, type)

                Log.d(TAG, "Loaded ${subredditsList.size} subscriptions from cache for user: $username")
                subredditsList
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load subscriptions cache for user: $username", e)
            null
        }
    }

    /**
     * Check if cache exists for user
     */
    suspend fun hasCachedSubscriptions(username: String): Boolean = withContext(Dispatchers.IO) {
        getCacheFile(username).exists()
    }

    /**
     * Clear cache for specific user
     */
    suspend fun clearCache(username: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val cacheFile = getCacheFile(username)
            if (cacheFile.exists()) {
                val deleted = cacheFile.delete()
                Log.d(TAG, "Cache cleared for user: $username, success: $deleted")
                deleted
            } else {
                Log.d(TAG, "No cache to clear for user: $username")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear cache for user: $username", e)
            false
        }
    }

    /**
     * Clear all cached subscriptions for all users
     */
    suspend fun clearAllCaches(): Boolean = withContext(Dispatchers.IO) {
        try {
            val cacheDir = File(context.filesDir, CACHE_DIR)
            if (cacheDir.exists() && cacheDir.isDirectory) {
                val files = cacheDir.listFiles() ?: emptyArray()
                var allDeleted = true
                files.forEach { file ->
                    if (!file.delete()) {
                        allDeleted = false
                        Log.w(TAG, "Failed to delete cache file: ${file.name}")
                    }
                }
                Log.d(TAG, "Cleared ${files.size} cache files, success: $allDeleted")
                allDeleted
            } else {
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear all caches", e)
            false
        }
    }
}
