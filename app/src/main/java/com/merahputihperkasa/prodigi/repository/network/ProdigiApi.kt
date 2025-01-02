package com.merahputihperkasa.prodigi.repository.network

import com.merahputihperkasa.prodigi.ProdigiApp
import com.merahputihperkasa.prodigi.models.BannerItemResult
import com.merahputihperkasa.prodigi.models.DigitalContentResult
import com.merahputihperkasa.prodigi.models.Submission
import com.merahputihperkasa.prodigi.models.SubmissionResult
import com.merahputihperkasa.prodigi.models.WorkSheetResult
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ProdigiApi {
    companion object {
        var BASE_URL = "https://${ProdigiApp.appModule.internalSourceDomain}"
    }

    @GET("/api/v2/links/{id}")
    suspend fun getDigitalContents(
        @Path("id") id: String,
        @Query("app") appId: String,
    ): DigitalContentResult

    @GET("api/v2/banners")
    suspend fun getBannerItems(): BannerItemResult

    @GET("/api/v2/quiz/{id}")
    suspend fun getWorksheetConf(
        @Path("id") id: String,
    ): WorkSheetResult

    @POST("/api/v2/quiz/{id}")
    suspend fun submitWorksheet(
        @Path("id") id: String,
        @Body body: Submission
    ): SubmissionResult
}