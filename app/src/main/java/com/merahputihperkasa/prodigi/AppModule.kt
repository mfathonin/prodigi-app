package com.merahputihperkasa.prodigi

import android.content.Context
import androidx.room.Room
import com.merahputihperkasa.prodigi.repository.local.ContentsDatabase
import com.merahputihperkasa.prodigi.repository.local.migrations.Migration1To2
import com.merahputihperkasa.prodigi.repository.local.migrations.Migration2To3
import com.merahputihperkasa.prodigi.repository.local.migrations.Migration3To4
import com.merahputihperkasa.prodigi.repository.network.ProdigiApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface AppModule {
    val db: ContentsDatabase
    val api: ProdigiApi
    val apiVersion: Int
    val internalSourceTag: String
    val internalSourceDomain: String
    val internalSourceModules: String
}

class AppReleaseModule(
    private val appContext: Context
): AppModule {
    override val apiVersion = 1
    override val internalSourceDomain = "mpp-hub.netlify.app"
    override val internalSourceModules = "/links"
    override val internalSourceTag: String
        get() = internalSourceDomain + internalSourceModules

    override val db: ContentsDatabase by lazy {
        Room.databaseBuilder(
            appContext,
            ContentsDatabase::class.java,
            "prodigi.db"
        )
            .addMigrations(Migration1To2, Migration2To3, Migration3To4)
            .fallbackToDestructiveMigration()
            .build()
    }

    private val interceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build()

    override val api: ProdigiApi by lazy {
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(ProdigiApi.BASE_URL)
            .client(client)
            .build()
            .create(ProdigiApi::class.java)
    }
}

class AppDebugModule(
    private val appContext: Context
): AppModule {
    override val apiVersion = 2
    override val internalSourceDomain = "develop--mpp-hub.netlify.app"
    override val internalSourceModules = "/links"
    override val internalSourceTag: String
        get() = internalSourceDomain + internalSourceModules

    override val db: ContentsDatabase by lazy {
        Room.databaseBuilder(
            appContext,
            ContentsDatabase::class.java,
            "prodigi.db"
        )
            .addMigrations(Migration1To2, Migration2To3, Migration3To4)
            .fallbackToDestructiveMigration()
            .build()
    }

    private val interceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build()

    override val api: ProdigiApi by lazy {
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(ProdigiApi.BASE_URL)
            .client(client)
            .build()
            .create(ProdigiApi::class.java)
    }
}