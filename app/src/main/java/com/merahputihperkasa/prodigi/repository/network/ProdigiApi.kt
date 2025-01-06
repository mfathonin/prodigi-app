package com.merahputihperkasa.prodigi.repository.network

import com.merahputihperkasa.prodigi.ProdigiApp
import com.merahputihperkasa.prodigi.models.BannerItemResult
import com.merahputihperkasa.prodigi.models.DigitalContentResult
import com.merahputihperkasa.prodigi.models.SubmissionBody
import com.merahputihperkasa.prodigi.models.SubmissionResult
import com.merahputihperkasa.prodigi.models.WorkSheetResult
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ProdigiApi {
    companion object {
        private val API_VERSION = "v${ProdigiApp.appModule.apiVersion}"
        private val API_HOST = ProdigiApp.appModule.internalSourceDomain
        var BASE_URL = "https://$API_HOST/api/$API_VERSION/"
    }

    @GET("links/{id}")
    suspend fun getDigitalContents(
        @Path("id") id: String,
        @Query("app") appId: String,
    ): DigitalContentResult

    @GET("banners")
    suspend fun getBannerItems(): BannerItemResult

    @GET("quiz/{id}")
    suspend fun getWorksheetConf(
        @Path("id") id: String,
    ): WorkSheetResult

    @POST("quiz/{id}")
    suspend fun submitWorksheet(
        @Path("id") id: String,
        @Body body: SubmissionBody
    ): SubmissionResult
}