package com.merahputihperkasa.prodigi.repository.network

import com.merahputihperkasa.prodigi.models.BannerItem
import com.merahputihperkasa.prodigi.models.DigitalContentResult
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface ProdigiApi {
    @GET("/api/v1/links/{id}")
    suspend fun getDigitalContents(
        @Path("id") id: String,
        @Query("app") appId: String,
    ): DigitalContentResult

    @GET("api/v1/banners")
    suspend fun getBannerItems(): List<BannerItem>

    companion object {
        const val BASE_URL = "https://mpp-hub.netlify.app/"
    }
}