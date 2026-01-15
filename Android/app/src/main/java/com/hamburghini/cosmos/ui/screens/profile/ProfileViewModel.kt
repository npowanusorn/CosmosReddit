package com.hamburghini.cosmos.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hamburghini.cosmos.data.manager.ProfileManager
import com.hamburghini.cosmos.data.model.AuthState
import com.hamburghini.cosmos.data.model.RedditAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileManager: ProfileManager
) : ViewModel() {

    val authState: StateFlow<AuthState> = profileManager.authState
    val storedAccounts: StateFlow<List<RedditAccount>> = profileManager.storedAccounts

    // Track account switching state
    private val _isSwitchingAccount = MutableStateFlow(false)
    val isSwitchingAccount: StateFlow<Boolean> = _isSwitchingAccount.asStateFlow()

    // Track last switch error
    private val _switchError = MutableStateFlow<String?>(null)
    val switchError: StateFlow<String?> = _switchError.asStateFlow()

    init {
        // Try to restore previous session if available
        profileManager.tryRestorePreviousSession()
    }

    /**
     * Logout current user
     */
    fun logout() {
        profileManager.logout()
    }

    /**
     * Switch to a different stored account with enhanced error handling
     */
    fun switchAccount(account: RedditAccount) {
        viewModelScope.launch {
            _isSwitchingAccount.value = true
            _switchError.value = null

            try {
                profileManager.switchAccount(account)

                // Wait a moment to ensure state is updated
                delay(300)

                // Check if switch was successful
                if (authState.value !is AuthState.LoggedIn ||
                    (authState.value as? AuthState.LoggedIn)?.account?.username != account.username) {
                    _switchError.value = "Failed to switch to account"
                }
            } catch (e: Exception) {
                _switchError.value = e.message ?: "Unknown error occurred"
            } finally {
                _isSwitchingAccount.value = false
            }
        }
    }

    /**
     * Remove an account from stored accounts
     */
    fun removeAccount(account: RedditAccount) {
        profileManager.removeAccount(account)
    }

    /**
     * Refresh current user's access token
     */
    fun refreshToken() {
        viewModelScope.launch {
            profileManager.refreshCurrentToken()
        }
    }

    /**
     * Clear switch error
     */
    fun clearSwitchError() {
        _switchError.value = null
    }

    // Helper functions for UI
    fun isLoggedIn(): Boolean = profileManager.isLoggedIn()

    fun getCurrentUsername(): String = profileManager.getDisplayUsername()

    fun canAccessAuthenticatedFeatures(): Boolean = profileManager.canAccessAuthenticatedFeatures()

    /**
     * Get authenticated API service if logged in
     */
    fun getAuthenticatedApiService() = profileManager.getAuthenticatedApiService()

    /**
     * Get number of stored accounts
     */
    fun getStoredAccountsCount(): Int = storedAccounts.value.size

    /**
     * Check if there are other accounts to switch to
     */
    fun hasOtherAccounts(): Boolean {
        val currentUsername = (authState.value as? AuthState.LoggedIn)?.account?.username
        return storedAccounts.value.any { it.username != currentUsername }
    }
}