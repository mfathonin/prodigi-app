package com.merahputihperkasa.prodigi.network

import com.merahputihperkasa.prodigi.models.DigitalContentResult
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProdigiApi {
    @GET("/links/{id}")
    suspend fun getDigitalContents(
        @Path("id") id: String,
        @Query("app") appId: String,
    ): DigitalContentResult

    companion object {
        const val BASE_URL = "https://mpp-hub.netlify.app/"
    }
}