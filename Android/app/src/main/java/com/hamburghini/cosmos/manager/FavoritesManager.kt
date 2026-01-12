package com.hamburghini.cosmos.manager

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "FavoritesManager"
        private const val FAVORITES_DIR = "favorites"
    }

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    /**
     * Get favorites file
     */
    private fun getFavoritesFile(username: String): File {
        val favoritesDir = File(context.filesDir, FAVORITES_DIR)
        if (!favoritesDir.exists()) {
            favoritesDir.mkdirs()
        }
        val sanitizedUsername = username.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        return File(favoritesDir, "favorites_$sanitizedUsername.json")
    }

    /**
     * Load all favorites
     */
    suspend fun getFavoritesForUser(username: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val favoritesFile = getFavoritesFile(username)
            if (!favoritesFile.exists()) {
                return@withContext emptyList()
            }

            FileReader(favoritesFile).use { reader ->
                val type = object : TypeToken<List<String>>() {}.type
                gson.fromJson<List<String>>(reader, type) ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load favorites", e)
            emptyList()
        }
    }

    /**
     * Save all favorites
     */
    private suspend fun saveAllFavorites(favorites: List<String>, username: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val favoritesFile = getFavoritesFile(username)
                FileWriter(favoritesFile).use { writer ->
                    gson.toJson(favorites, writer)
                }
                Log.d(TAG, "Saved ${favorites.size} favorites")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save favorites", e)
                false
            }
        }

    /**
     * Add subreddit to favorites
     */
    suspend fun addFavorite(
        username: String,
        subreddit: String
    ): Boolean {
        val allFavorites = getFavoritesForUser(username).toMutableList()

        // Check if already favorited
        if (allFavorites.contains(subreddit)) {
            Log.d(TAG, "Subreddit $subreddit already favorited by $username")
            return false
        }

        allFavorites.add(subreddit)
        val success = saveAllFavorites(allFavorites, username)

        if (success) {
            Log.d(TAG, "Added favorite: $subreddit for user: $username")
        }

        return success
    }

    /**
     * Remove subreddit from favorites
     */
    suspend fun removeFavorite(
        username: String,
        subredditId: String
    ): Boolean {
        val allFavorites = getFavoritesForUser(username).toMutableList()
        val removed = allFavorites.removeAll {
            it == subredditId
        }

        if (removed) {
            val success = saveAllFavorites(allFavorites, username)
            if (success) {
                Log.d(TAG, "Removed favorite: $subredditId for user: $username")
            }
            return success
        }

        return false
    }

    /**
     * Check if subreddit is favorited by user
     */
    suspend fun isFavorited(username: String, subredditId: String): Boolean {
        return getFavoritesForUser(username).any {
            it == subredditId
        }
    }

    /**
     * Get favorite count for user
     */
    suspend fun getFavoriteCount(username: String): Int {
        return getFavoritesForUser(username).size
    }

    /**
     * Update favorite sort order
     */
    suspend fun updateSortOrder(
        username: String,
        favorites: List<String>
    ): Boolean {
        val allFavorites = getFavoritesForUser(username).toMutableList()

        // Remove old favorites for this user
        allFavorites.clear()

        // Add updated favorites with new sort order
        allFavorites.addAll(favorites)

        return saveAllFavorites(allFavorites, username)
    }

    /**
     * Clear all favorites for user
     */
    suspend fun clearFavoritesForUser(username: String): Boolean {
        val allFavorites = getFavoritesForUser(username).toMutableList()
        allFavorites.clear()

        Log.d(TAG, "Cleared all favorites for user: $username")
        return saveAllFavorites(allFavorites, username)
    }
}