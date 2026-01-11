package com.hamburghini.cosmos.model

/**
 * Sealed class representing the different authentication states in the app
 */
sealed class AuthState {
    /**
     * User is not logged in - anonymous browsing mode
     */
    data object NotLoggedIn : AuthState()

    /**
     * User is in the process of logging in
     */
    data object LoggingIn : AuthState()

    /**
     * User is successfully logged in with account details
     * @param account The active Reddit account
     * @param userInfo Additional user information from Reddit
     */
    data class LoggedIn(
        val account: RedditAccount,
        val userInfo: UserInfo
    ) : AuthState()

    /**
     * Authentication failed or session expired
     * @param error The error message describing what went wrong
     * @param canRetry Whether the user can retry the authentication
     */
    data class AuthError(
        val error: String,
        val canRetry: Boolean = true
    ) : AuthState()
}