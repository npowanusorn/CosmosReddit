package com.hamburghini.cosmos.manager

import android.app.Activity
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
import androidx.core.content.edit

/**
 * Enhanced ProfileManager with subscription cache integration
 * Handles cache cleanup on account changes
 */
@Singleton
class ProfileManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val subscriptionCacheManager: SubscriptionCacheManager
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

    private lateinit var authManager: RedditAuthManager

    private val _authState = MutableStateFlow<AuthState>(AuthState.NotLoggedIn)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _storedAccounts = MutableStateFlow<List<RedditAccount>>(emptyList())
    val storedAccounts: StateFlow<List<RedditAccount>> = _storedAccounts.asStateFlow()

    init {
        loadStoredAccounts()
        loadLastAuthState()
    }

    fun setAuthManager(authManager: RedditAuthManager) {
        this.authManager = authManager
    }

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

    fun getAuthenticatedApiService(): RedditApiService? {
        val account = getCurrentAccount() ?: return null
        return createAuthenticatedApiService(account.accessToken)
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

    fun tryRestorePreviousSession() {
        val activeUsername = prefs.getString(KEY_ACTIVE_ACCOUNT_USERNAME, null)
        if (activeUsername != null) {
            val account = _storedAccounts.value.find { it.username == activeUsername }
            if (account != null && account.refreshToken.isNotBlank()) {
                _authState.value = AuthState.LoggingIn

                scope.launch {
                    when (val result = authManager.refreshAccessToken(account.refreshToken)) {
                        is RedditAuthManager.AuthResult.Success -> {
                            completeLogin(result.account, result.userInfo)
                        }
                        is RedditAuthManager.AuthResult.Error -> {
                            _authState.value = AuthState.NotLoggedIn
                        }
                    }
                }
            } else {
                _authState.value = AuthState.NotLoggedIn
            }
        } else {
            _authState.value = AuthState.NotLoggedIn
        }
    }

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

    private fun loadLastAuthState() {
        val activeUsername = prefs.getString(KEY_ACTIVE_ACCOUNT_USERNAME, null)
        _authState.value = if (activeUsername != null && _storedAccounts.value.any { it.username == activeUsername }) {
            AuthState.LoggingIn
        } else {
            AuthState.NotLoggedIn
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