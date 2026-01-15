package com.hamburghini.cosmos.data.manager

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hamburghini.cosmos.core.auth.RedditAuthManager
import com.hamburghini.cosmos.core.network.RedditApiService
import com.hamburghini.cosmos.core.network.RetrofitClient
import com.hamburghini.cosmos.data.model.AuthState
import com.hamburghini.cosmos.data.model.RedditAccount
import com.hamburghini.cosmos.data.model.UserInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

/**
 * Enhanced ProfileManager with RetrofitClient integration
 * Implements token provider and refresher for automatic token management
 * Optimized for fast initialization
 */
@Singleton
class ProfileManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val subscriptionCacheManager: SubscriptionCacheManager
) : RetrofitClient.TokenProvider, RetrofitClient.TokenRefresher {

    companion object {
        private const val TAG = "ProfileManager"
        private const val PREFS_NAME = "reddit_profile_prefs"
        private const val KEY_ACCOUNTS = "stored_accounts"
        private const val KEY_ACTIVE_ACCOUNT_USERNAME = "active_account_username"
        private const val KEY_LAST_AUTH_STATE = "last_auth_state"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var authManager: RedditAuthManager

    private val _authState = MutableStateFlow<AuthState>(AuthState.NotLoggedIn)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _storedAccounts = MutableStateFlow<List<RedditAccount>>(emptyList())
    val storedAccounts: StateFlow<List<RedditAccount>> = _storedAccounts.asStateFlow()

    init {
        loadStoredAccounts()
        initializeAuthState()

        // Register this ProfileManager as the token handler for RetrofitClient
        RetrofitClient.setTokenHandlers(this, this)
    }

    /**
     * Initialize auth state quickly
     * If there's an active account, start token refresh in background
     */
    private fun initializeAuthState() {
        val activeUsername = prefs.getString(KEY_ACTIVE_ACCOUNT_USERNAME, null)

        if (activeUsername != null) {
            val account = _storedAccounts.value.find { it.username == activeUsername }

            if (account != null && account.refreshToken.isNotBlank()) {
                // Set to LoggingIn temporarily
                _authState.value = AuthState.LoggingIn

                // Restore session in background - non-blocking
                scope.launch {
                    restoreSessionInBackground()
                }
            } else {
                // No valid account found, set to NotLoggedIn immediately
                _authState.value = AuthState.NotLoggedIn
            }
        } else {
            // No active account, set to NotLoggedIn immediately
            _authState.value = AuthState.NotLoggedIn
        }
    }

    fun setAuthManager(authManager: RedditAuthManager) {
        this.authManager = authManager
    }

    // ==================== TokenProvider Implementation ====================

    override fun getAccessToken(): String? {
        return getCurrentAccount()?.accessToken
    }

    // ==================== TokenRefresher Implementation ====================

    override suspend fun refreshToken(): RetrofitClient.RefreshResult {
        val account = getCurrentAccount() ?: return RetrofitClient.RefreshResult.Failure
        val refreshToken = account.refreshToken

        if (refreshToken.isBlank()) {
            return RetrofitClient.RefreshResult.Failure
        }

        return try {
            // Use RedditAuthManager to refresh token
            if (::authManager.isInitialized) {
                when (val result = authManager.refreshAccessToken(refreshToken)) {
                    is RedditAuthManager.AuthResult.Success -> {
                        // Update account with new token
                        completeLogin(result.account, result.userInfo)
                        RetrofitClient.RefreshResult.Success(result.account.accessToken)
                    }
                    is RedditAuthManager.AuthResult.Error -> {
                        handleLoginError(result.message)
                        RetrofitClient.RefreshResult.Failure
                    }
                }
            } else {
                RetrofitClient.RefreshResult.Failure
            }
        } catch (e: Exception) {
            RetrofitClient.RefreshResult.Failure
        }
    }

    // ==================== Public API ====================

    fun isLoggedIn(): Boolean {
        return _authState.value is AuthState.LoggedIn
    }

    fun getCurrentAccount(): RedditAccount? {
        return when (val state = _authState.value) {
            is AuthState.LoggedIn -> state.account
            else -> null
        }
    }

    fun getCurrentUserInfo(): UserInfo? {
        return when (val state = _authState.value) {
            is AuthState.LoggedIn -> state.userInfo
            else -> null
        }
    }

    fun getDisplayUsername(): String {
        return getCurrentAccount()?.username ?: "Anonymous"
    }

    fun getUserAvatarUrl(): String? {
        return getCurrentUserInfo()?.iconImg?.takeIf { it.isNotBlank() }
    }

    fun canAccessAuthenticatedFeatures(): Boolean {
        return isLoggedIn() && getCurrentAccount()?.accessToken?.isNotBlank() == true
    }

    fun startLogin(activity: Activity? = null) {
        _authState.value = AuthState.LoggingIn
        saveAuthState()

        if (::authManager.isInitialized) {
            if (activity != null) {
                authManager.startOAuthFlow(activity)
            } else {
                handleLoginError("Activity context required for login")
            }
        } else {
            handleLoginError("Authentication system not ready")
        }
    }

    suspend fun completeLogin(account: RedditAccount, userInfo: UserInfo) {
        try {
            val updatedAccount = account.copy(isActive = true)
            updateStoredAccount(updatedAccount)
            _authState.value = AuthState.LoggedIn(updatedAccount, userInfo)
            saveAuthState()
        } catch (e: Exception) {
            _authState.value = AuthState.AuthError("Failed to complete login: ${e.message}")
            saveAuthState()
        }
    }

    fun handleLoginError(error: String, canRetry: Boolean = true) {
        _authState.value = AuthState.AuthError(error, canRetry)
        saveAuthState()
    }

    /**
     * Logout current user and optionally clear their cache
     */
    fun logout(clearCache: Boolean = false) {
        getCurrentAccount()?.let { account ->
            val inactiveAccount = account.copy(isActive = false)
            updateStoredAccount(inactiveAccount)

            // Clear cache for this user if requested
            if (clearCache) {
                scope.launch {
                    subscriptionCacheManager.clearCache(account.username)
                }
            }
        }

        _authState.value = AuthState.NotLoggedIn
        prefs.edit { remove(KEY_ACTIVE_ACCOUNT_USERNAME) }
        saveAuthState()
    }

    suspend fun switchAccount(account: RedditAccount) {
        if (account.accessToken.isBlank()) {
            handleLoginError("Account has invalid access token")
            return
        }

        try {
            _authState.value = AuthState.LoggingIn
            saveAuthState()

            if (::authManager.isInitialized && account.refreshToken.isNotBlank()) {
                when (val result = authManager.refreshAccessToken(account.refreshToken)) {
                    is RedditAuthManager.AuthResult.Success -> {
                        completeLogin(result.account, result.userInfo)
                    }
                    is RedditAuthManager.AuthResult.Error -> {
                        handleLoginError("Failed to switch account: ${result.message}")
                    }
                }
            } else {
                handleLoginError("Cannot switch to account - refresh token unavailable")
            }
        } catch (e: Exception) {
            handleLoginError("Failed to switch account: ${e.message}")
        }
    }

    /**
     * Remove an account and clear its cache
     */
    fun removeAccount(account: RedditAccount, clearCache: Boolean = true) {
        val currentAccounts = _storedAccounts.value.toMutableList()
        currentAccounts.removeAll { it.username == account.username }

        _storedAccounts.value = currentAccounts
        saveStoredAccounts()

        // Clear cache for removed account
        if (clearCache) {
            scope.launch {
                subscriptionCacheManager.clearCache(account.username)
            }
        }

        if (getCurrentAccount()?.username == account.username) {
            logout(clearCache = false) // Already cleared above
        }
    }

    /**
     * Get authenticated API service from RetrofitClient
     */
    fun getAuthenticatedApiService(): RedditApiService? {
        return if (isLoggedIn()) {
            RetrofitClient.authenticatedRedditApiService
        } else {
            null
        }
    }

    suspend fun refreshCurrentToken(): Boolean {
        val account = getCurrentAccount() ?: return false
        if (account.refreshToken.isBlank() || !::authManager.isInitialized) return false

        return try {
            when (val result = authManager.refreshAccessToken(account.refreshToken)) {
                is RedditAuthManager.AuthResult.Success -> {
                    completeLogin(result.account, result.userInfo)
                    true
                }
                is RedditAuthManager.AuthResult.Error -> {
                    handleLoginError("Token refresh failed: ${result.message}")
                    false
                }
            }
        } catch (e: Exception) {
            handleLoginError("Token refresh error: ${e.message}")
            false
        }
    }

    /**
     * Restore previous session in background
     * Called during init to avoid blocking
     */
    private suspend fun restoreSessionInBackground() {
        val activeUsername = prefs.getString(KEY_ACTIVE_ACCOUNT_USERNAME, null)
        if (activeUsername != null) {
            val account = _storedAccounts.value.find { it.username == activeUsername }
            if (account != null && account.refreshToken.isNotBlank() && ::authManager.isInitialized) {
                when (val result = authManager.refreshAccessToken(account.refreshToken)) {
                    is RedditAuthManager.AuthResult.Success -> {
                        completeLogin(result.account, result.userInfo)
                    }
                    is RedditAuthManager.AuthResult.Error -> {
                        _authState.value = AuthState.NotLoggedIn
                    }
                }
            } else {
                _authState.value = AuthState.NotLoggedIn
            }
        } else {
            _authState.value = AuthState.NotLoggedIn
        }
    }

    // ==================== Private Helpers ====================

    private fun loadStoredAccounts() {
        val accountsJson = prefs.getString(KEY_ACCOUNTS, null)
        if (accountsJson != null) {
            try {
                val type = object : TypeToken<List<RedditAccount>>() {}.type
                val accounts: List<RedditAccount> = gson.fromJson(accountsJson, type)
                _storedAccounts.value = accounts
            } catch (e: Exception) {
                _storedAccounts.value = emptyList()
            }
        }
    }

    private fun saveStoredAccounts() {
        val accountsJson = gson.toJson(_storedAccounts.value)
        prefs.edit().putString(KEY_ACCOUNTS, accountsJson).apply()
    }

    private fun updateStoredAccount(account: RedditAccount) {
        val currentAccounts = _storedAccounts.value.toMutableList()
        currentAccounts.removeAll { it.username == account.username }
        currentAccounts.add(account)

        _storedAccounts.value = currentAccounts
        saveStoredAccounts()

        if (account.isActive) {
            prefs.edit().putString(KEY_ACTIVE_ACCOUNT_USERNAME, account.username).apply()
        }
    }

    private fun saveAuthState() {
        val isLoggedIn = _authState.value is AuthState.LoggedIn
        prefs.edit().putBoolean("was_logged_in", isLoggedIn).apply()
    }
}