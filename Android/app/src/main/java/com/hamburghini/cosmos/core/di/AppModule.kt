package com.hamburghini.cosmos.core.di

import android.content.Context
import com.hamburghini.cosmos.core.auth.RedditAuthManager
import com.hamburghini.cosmos.manager.FavoritesManager
import com.hamburghini.cosmos.manager.ProfileManager
import com.hamburghini.cosmos.manager.SubscriptionCacheManager
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
    fun provideRedditRepository(
        profileManager: ProfileManager,
        subscriptionCacheManager: SubscriptionCacheManager
    ): RedditRepository {
        return RedditRepository(profileManager, subscriptionCacheManager)
    }

    @Provides
    @Singleton
    fun provideProfileManager(
        @ApplicationContext context: Context,
        subscriptionCacheManager: SubscriptionCacheManager
    ): ProfileManager {
        return ProfileManager(context, subscriptionCacheManager)
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

    @Provides
    @Singleton
    fun provideSubscriptionCacheManager(
        @ApplicationContext context: Context
    ): SubscriptionCacheManager {
        return SubscriptionCacheManager(context)
    }

    @Provides
    @Singleton
    fun provideFavoritesManager(
        @ApplicationContext context: Context
    ): FavoritesManager {
        return FavoritesManager(context)
    }
}