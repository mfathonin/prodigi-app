package com.merahputihperkasa.prodigi

import android.app.Application

class ProdigiApp: Application() {
    companion object {
        lateinit var appModule: AppModule

    }

    override fun onCreate() {
        super.onCreate()

        // Initialize the app module base on active build variant
        val buildType = BuildConfig.BUILD_TYPE
        @Suppress("KotlinConstantConditions")
        appModule = if (buildType == "debug") {
            AppDebugModule(this)
        } else {
            AppReleaseModule(this)
        }
    }
}