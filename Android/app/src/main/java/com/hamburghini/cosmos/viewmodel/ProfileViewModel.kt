package com.hamburghini.cosmos.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hamburghini.cosmos.manager.ProfileManager
import com.hamburghini.cosmos.model.AuthState
import com.hamburghini.cosmos.model.RedditAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileManager: ProfileManager
) : ViewModel() {

    val authState: StateFlow<AuthState> = profileManager.authState
    val storedAccounts: StateFlow<List<RedditAccount>> = profileManager.storedAccounts

    init {
        // Try to restore previous session if available
        profileManager.tryRestorePreviousSession()
    }

    /**
     * Start OAuth login flow - this will open Chrome Custom Tab
     * Requires Activity context to launch Chrome Custom Tab
     */
    fun startLogin(activity: Activity) {
        profileManager.startLogin(activity)
    }

    /**
     * Retry login after error
     * Requires Activity context to launch Chrome Custom Tab
     */
    fun retryLogin(activity: Activity) {
        startLogin(activity)
    }

    /**
     * Logout current user
     */
    fun logout() {
        profileManager.logout()
    }

    /**
     * Switch to a different stored account
     */
    fun switchAccount(account: RedditAccount) {
        viewModelScope.launch {
            profileManager.switchAccount(account)
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

    // Helper functions for UI
    fun isLoggedIn(): Boolean = profileManager.isLoggedIn()

    fun getCurrentUsername(): String = profileManager.getDisplayUsername()

    fun canAccessAuthenticatedFeatures(): Boolean = profileManager.canAccessAuthenticatedFeatures()

    /**
     * Get authenticated API service if logged in
     */
    fun getAuthenticatedApiService() = profileManager.getAuthenticatedApiService()
}