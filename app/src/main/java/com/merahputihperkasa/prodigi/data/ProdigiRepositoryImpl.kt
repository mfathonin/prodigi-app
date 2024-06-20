package com.merahputihperkasa.prodigi.data

import android.content.Context
import com.merahputihperkasa.prodigi.models.Content
import com.merahputihperkasa.prodigi.network.ApiResult
import com.merahputihperkasa.prodigi.network.ProdigiApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException

class ProdigiRepositoryImpl(
    private val api: ProdigiApi,
) : ProdigiRepository {
    override suspend fun getDigitalContents(
        context: Context,
        id: String,
    ): Flow<ApiResult<List<Content>>> {
        return flow {
            emit(ApiResult.Loading())

            val packageName = context.packageName

            val digitalContents = try {
                api.getDigitalContents(id, packageName)
            } catch (e: IOException) {
                emit(ApiResult.Error(message = "Error loading content"))
                return@flow
            } catch (e: Exception) {
                emit(ApiResult.Error(message = "Error loading content"))
                return@flow
            }

            emit(ApiResult.Success(digitalContents.contents))
        }
    }
}