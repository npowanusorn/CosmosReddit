package com.hamburghini.cosmos

import android.app.Application
import com.hamburghini.playerplus.PlayerPlusSDK
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CosmosApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        PlayerPlusSDK.initialize(applicationContext)
    }
}