package com.merahputihperkasa.prodigi.repository

import android.content.Context
import com.merahputihperkasa.prodigi.models.Content
import com.merahputihperkasa.prodigi.models.ContentEntity
import kotlinx.coroutines.flow.Flow

interface ProdigiRepository {
    suspend fun getDigitalContents(context: Context, id: String): Flow<LoadDataStatus<List<Content>>>
    suspend fun getFilteredContents(filter: String): Flow<LoadDataStatus<List<ContentEntity>>>
}

