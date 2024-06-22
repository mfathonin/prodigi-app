package com.merahputihperkasa.prodigi

import android.content.Context
import androidx.room.Room
import com.merahputihperkasa.prodigi.repository.local.ContentsDatabase
import com.merahputihperkasa.prodigi.repository.network.ProdigiApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface AppModule {
    val db: ContentsDatabase
    val api: ProdigiApi
    val internalSourceTag: String
    val internalSourceDomain: String
    val internalSourceModules: String
}

class AppModuleImpl(
    private val appContext: Context
): AppModule {
    override val internalSourceDomain: String
        get() = "mpp-hub.netlify.app"
    override val internalSourceModules: String
        get() = "/links"
    override val internalSourceTag: String
        get() = internalSourceDomain + internalSourceModules

    override val db: ContentsDatabase by lazy {
        Room.databaseBuilder(
            appContext,
            ContentsDatabase::class.java,
            "contents.db"
        ).build()
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