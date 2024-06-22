package com.merahputihperkasa.prodigi

import android.app.Application

class ProdigiApp: Application() {
    companion object {
        lateinit var appModule: AppModule

    }

    override fun onCreate() {
        super.onCreate()
        appModule = AppModuleImpl(this)
    }
}