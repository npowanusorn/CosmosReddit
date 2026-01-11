package com.hamburghini.cosmos.util

import com.hamburghini.cosmos.model.RedditAccount
import com.hamburghini.cosmos.model.UserInfo
import java.text.NumberFormat
import java.util.Locale

object AccountUtils {

    /**
     * Format karma count with abbreviations (K, M)
     */
    fun formatKarma(karma: Int): String {
        return when {
            karma >= 1_000_000 -> String.format(Locale.getDefault(), "%.1fM", karma / 1_000_000.0)
            karma >= 10_000 -> String.format(Locale.getDefault(), "%.1fk", karma / 1_000.0)
            karma >= 1_000 -> String.format(Locale.getDefault(), "%.1fk", karma / 1_000.0)
            else -> NumberFormat.getNumberInstance(Locale.getDefault()).format(karma)
        }
    }

    /**
     * Get display name with 'u/' prefix
     */
    fun getDisplayUsername(username: String): String {
        return "u/$username"
    }

    /**
     * Get initials for avatar (1-2 characters)
     */
    fun getInitials(username: String): String {
        return username.take(1).uppercase()
    }

    /**
     * Validate if account has valid credentials
     */
    fun isAccountValid(account: RedditAccount): Boolean {
        return account.accessToken.isNotBlank() && account.username.isNotBlank()
    }

    /**
     * Check if account needs token refresh (basic check)
     */
    fun needsTokenRefresh(account: RedditAccount): Boolean {
        // Check if account is older than 50 minutes (tokens expire in 1 hour)
        val timeSinceAdded = System.currentTimeMillis() - account.addedTimestamp
        return timeSinceAdded > 50 * 60 * 1000 // 50 minutes
    }

    /**
     * Get account age description
     */
    fun getAccountAgeDescription(createdUtc: Long): String {
        val currentTime = System.currentTimeMillis() / 1000
        val ageInSeconds = currentTime - createdUtc

        return when {
            ageInSeconds < 60 -> "Just now"
            ageInSeconds < 3600 -> "${ageInSeconds / 60} minutes old"
            ageInSeconds < 86400 -> "${ageInSeconds / 3600} hours old"
            ageInSeconds < 2592000 -> "${ageInSeconds / 86400} days old"
            ageInSeconds < 31536000 -> "${ageInSeconds / 2592000} months old"
            else -> "${ageInSeconds / 31536000} years old"
        }
    }

    /**
     * Get total karma from user info
     */
    fun getTotalKarma(userInfo: UserInfo): Int {
        return userInfo.totalKarma.coerceAtLeast(
            userInfo.linkKarma + userInfo.commentKarma
        )
    }

    /**
     * Sort accounts by activity (active first, then by username)
     */
    fun sortAccounts(accounts: List<RedditAccount>): List<RedditAccount> {
        return accounts.sortedWith(
            compareByDescending<RedditAccount> { it.isActive }
                .thenBy { it.username.lowercase() }
        )
    }

    /**
     * Check if username is valid Reddit format
     */
    fun isValidUsername(username: String): Boolean {
        // Reddit usernames: 3-20 characters, alphanumeric + underscore/hyphen
        val regex = Regex("^[a-zA-Z0-9_-]{3,20}$")
        return regex.matches(username)
    }

    /**
     * Mask sensitive token for logging
     */
    fun maskToken(token: String): String {
        if (token.length <= 8) return "***"
        return "${token.take(4)}...${token.takeLast(4)}"
    }

    /**
     * Get account summary for debugging
     */
    fun getAccountSummary(account: RedditAccount): String {
        return "Account(username=${account.username}, " +
                "hasAccessToken=${account.accessToken.isNotBlank()}, " +
                "hasRefreshToken=${account.refreshToken.isNotBlank()}, " +
                "isActive=${account.isActive})"
    }
}