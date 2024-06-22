package com.merahputihperkasa.prodigi.repository

import android.content.Context
import com.merahputihperkasa.prodigi.models.Content
import com.merahputihperkasa.prodigi.repository.network.ApiResult
import kotlinx.coroutines.flow.Flow

interface ProdigiRepository {
    suspend fun getDigitalContents(context: Context, id: String): Flow<ApiResult<List<Content>>>
}

