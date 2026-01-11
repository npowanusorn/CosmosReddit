package com.hamburghini.cosmos.di

import android.content.Context
import com.hamburghini.cosmos.auth.RedditAuthManager
import com.hamburghini.cosmos.manager.ProfileManager
import com.hamburghini.cosmos.repository.RedditRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRedditRepository(profileManager: ProfileManager): RedditRepository {
        return RedditRepository(profileManager)
    }

    @Provides
    @Singleton
    fun provideProfileManager(@ApplicationContext context: Context): ProfileManager {
        return ProfileManager(context)
    }

    @Provides
    @Singleton
    fun provideRedditAuthManager(
        @ApplicationContext context: Context,
        profileManager: ProfileManager
    ): RedditAuthManager {
        val authManager = RedditAuthManager(context, profileManager)
        // Set the auth manager in profile manager to avoid circular dependency
        profileManager.setAuthManager(authManager)
        return authManager
    }
}