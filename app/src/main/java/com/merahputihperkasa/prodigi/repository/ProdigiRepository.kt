package com.merahputihperkasa.prodigi.repository

import android.content.Context
import com.merahputihperkasa.prodigi.models.BannerItem
import com.merahputihperkasa.prodigi.models.Content
import com.merahputihperkasa.prodigi.models.ContentEntity
import kotlinx.coroutines.flow.Flow

interface ProdigiRepository {
    suspend fun getDigitalContents(id: String): Flow<LoadDataStatus<List<Content>>>
    suspend fun getFilteredContents(filter: String, forceRefresh: Boolean): Flow<LoadDataStatus<List<ContentEntity>>>
    suspend fun getBannerItems(forceRefresh: Boolean): Flow<LoadDataStatus<List<BannerItem>>>
}

