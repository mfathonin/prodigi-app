package com.merahputihperkasa.prodigi.repository

import android.content.Context
import com.merahputihperkasa.prodigi.models.BannerItem
import com.merahputihperkasa.prodigi.models.Content
import com.merahputihperkasa.prodigi.models.ContentEntity
import com.merahputihperkasa.prodigi.models.Submission
import com.merahputihperkasa.prodigi.models.WorkSheet
import kotlinx.coroutines.flow.Flow

interface ProdigiRepository {
    suspend fun getDigitalContents(id: String): Flow<LoadDataStatus<Content>>
    suspend fun getFilteredContents(filter: String, forceRefresh: Boolean): Flow<LoadDataStatus<List<ContentEntity>>>
    suspend fun getBannerItems(forceRefresh: Boolean): Flow<LoadDataStatus<List<BannerItem>>>
    suspend fun getWorkSheetConfig(id: String, forceRefresh: Boolean): Flow<LoadDataStatus<WorkSheet>>
    suspend fun getSubmisisonById(id: Int): Flow<LoadDataStatus<Submission?>>
    suspend fun saveProfile(workSheetId: String, name: String, idNumber: String, className: String, schoolName: String): Int
}

