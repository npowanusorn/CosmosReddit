package com.hamburghini.cosmos.manager

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hamburghini.cosmos.auth.RedditAuthManager
import com.hamburghini.cosmos.model.AuthState
import com.hamburghini.cosmos.model.RedditAccount
import com.hamburghini.cosmos.model.UserInfo
import com.hamburghini.cosmos.network.RedditApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "reddit_profile_prefs"
        private const val KEY_ACCOUNTS = "stored_accounts"
        private const val KEY_ACTIVE_ACCOUNT_USERNAME = "active_account_username"
        private const val KEY_LAST_AUTH_STATE = "last_auth_state"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Lazy-initialized to avoid circular dependency
    private lateinit var authManager: RedditAuthManager

    private val _authState = MutableStateFlow<AuthState>(AuthState.NotLoggedIn)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _storedAccounts = MutableStateFlow<List<RedditAccount>>(emptyList())
    val storedAccounts: StateFlow<List<RedditAccount>> = _storedAccounts.asStateFlow()

    init {
        loadStoredAccounts()
        loadLastAuthState()
    }

    /**
     * Initialize with AuthManager (called after dependency injection)
     */
    fun setAuthManager(authManager: RedditAuthManager) {
        this.authManager = authManager
    }

    /**
     * Check if user is currently logged in
     */
    fun isLoggedIn(): Boolean {
        return _authState.value is AuthState.LoggedIn
    }

    /**
     * Get current active account if logged in
     */
    fun getCurrentAccount(): RedditAccount? {
        return when (val state = _authState.value) {
            is AuthState.LoggedIn -> state.account
            else -> null
        }
    }

    /**
     * Get current user info if logged in
     */
    fun getCurrentUserInfo(): UserInfo? {
        return when (val state = _authState.value) {
            is AuthState.LoggedIn -> state.userInfo
            else -> null
        }
    }

    /**
     * Get username for display (returns "Anonymous" if not logged in)
     */
    fun getDisplayUsername(): String {
        return getCurrentAccount()?.username ?: "Anonymous"
    }

    /**
     * Get user avatar URL if available
     */
    fun getUserAvatarUrl(): String? {
        return getCurrentUserInfo()?.iconImg?.takeIf { it.isNotBlank() }
    }

    /**
     * Check if user can access authenticated features
     */
    fun canAccessAuthenticatedFeatures(): Boolean {
        return isLoggedIn() && getCurrentAccount()?.accessToken?.isNotBlank() == true
    }

    /**
     * Start login process using OAuth
     */
    fun startLogin(activity: android.app.Activity? = null) {
        _authState.value = AuthState.LoggingIn
        saveAuthState()

        // Start OAuth flow using RedditAuthManager
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

    /**
     * Complete login with account and user info (called by OAuth callback)
     */
    suspend fun completeLogin(account: RedditAccount, userInfo: UserInfo) {
        try {
            // Update account as active
            val updatedAccount = account.copy(isActive = true)

            // Update stored accounts
            updateStoredAccount(updatedAccount)

            // Set auth state to logged in
            _authState.value = AuthState.LoggedIn(updatedAccount, userInfo)

            saveAuthState()
        } catch (e: Exception) {
            _authState.value = AuthState.AuthError("Failed to complete login: ${e.message}")
            saveAuthState()
        }
    }

    /**
     * Handle login failure
     */
    fun handleLoginError(error: String, canRetry: Boolean = true) {
        _authState.value = AuthState.AuthError(error, canRetry)
        saveAuthState()
    }

    /**
     * Logout current user
     */
    fun logout() {
        // Mark current account as inactive
        getCurrentAccount()?.let { account ->
            val inactiveAccount = account.copy(isActive = false)
            updateStoredAccount(inactiveAccount)
        }

        _authState.value = AuthState.NotLoggedIn

        // Clear active account preference
        prefs.edit().remove(KEY_ACTIVE_ACCOUNT_USERNAME).apply()
        saveAuthState()
    }

    /**
     * Switch to a different stored account
     */
    suspend fun switchAccount(account: RedditAccount) {
        if (account.accessToken.isBlank()) {
            handleLoginError("Account has invalid access token")
            return
        }

        try {
            _authState.value = AuthState.LoggingIn
            saveAuthState()

            // Try to refresh the token first
            if (::authManager.isInitialized && account.refreshToken.isNotBlank()) {
                val result = authManager.refreshAccessToken(account.refreshToken)
                when (result) {
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
     * Remove an account from stored accounts
     */
    fun removeAccount(account: RedditAccount) {
        val currentAccounts = _storedAccounts.value.toMutableList()
        currentAccounts.removeAll { it.username == account.username }

        _storedAccounts.value = currentAccounts
        saveStoredAccounts()

        // If this was the active account, logout
        if (getCurrentAccount()?.username == account.username) {
            logout()
        }
    }

    /**
     * Get authenticated API service for current user
     */
    fun getAuthenticatedApiService(): RedditApiService? {
        val account = getCurrentAccount() ?: return null
        return createAuthenticatedApiService(account.accessToken)
    }

    /**
     * Refresh current user's access token if possible
     */
    suspend fun refreshCurrentToken(): Boolean {
        val account = getCurrentAccount() ?: return false
        if (account.refreshToken.isBlank() || !::authManager.isInitialized) return false

        return try {
            val result = authManager.refreshAccessToken(account.refreshToken)
            when (result) {
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
     * Try to restore previous session on app start
     */
    fun tryRestorePreviousSession() {
        val activeUsername = prefs.getString(KEY_ACTIVE_ACCOUNT_USERNAME, null)
        if (activeUsername != null) {
            val account = _storedAccounts.value.find { it.username == activeUsername }
            if (account != null && account.refreshToken.isNotBlank()) {
                // Try to refresh the token in background
                scope.launch {
                    switchAccount(account)
                }
            }
        }
    }

    // Private helper methods
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

        // Remove existing account with same username
        currentAccounts.removeAll { it.username == account.username }

        // Add updated account
        currentAccounts.add(account)

        _storedAccounts.value = currentAccounts
        saveStoredAccounts()

        // Update active account preference
        if (account.isActive) {
            prefs.edit().putString(KEY_ACTIVE_ACCOUNT_USERNAME, account.username).apply()
        }
    }

    private fun loadLastAuthState() {
        // Don't auto-restore login state - require fresh authentication
        // But we can check if there were stored accounts
        val activeUsername = prefs.getString(KEY_ACTIVE_ACCOUNT_USERNAME, null)
        if (activeUsername != null && _storedAccounts.value.any { it.username == activeUsername }) {
            // There was a previously active account, but start logged out for security
            _authState.value = AuthState.NotLoggedIn
        }
    }

    private fun saveAuthState() {
        val isLoggedIn = _authState.value is AuthState.LoggedIn
        prefs.edit().putBoolean("was_logged_in", isLoggedIn).apply()
    }

    private fun createAuthenticatedApiService(accessToken: String): RedditApiService {
        val authInterceptor = okhttp3.Interceptor { chain ->
            val originalRequest = chain.request()
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .header("User-Agent", "android:com.hamburghini.cosmos:v1.0 (by /u/YourUsername)")
                .build()
            chain.proceed(newRequest)
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://oauth.reddit.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RedditApiService::class.java)
    }
}