package com.merahputihperkasa.prodigi.repository

import android.content.Context
import android.util.Log
import com.merahputihperkasa.prodigi.AppModule
import com.merahputihperkasa.prodigi.models.Collection
import com.merahputihperkasa.prodigi.models.Content
import com.merahputihperkasa.prodigi.models.Link
import com.merahputihperkasa.prodigi.repository.local.ContentsDatabase
import com.merahputihperkasa.prodigi.repository.network.ApiResult
import com.merahputihperkasa.prodigi.repository.network.ProdigiApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException

class ProdigiRepositoryImpl(
    private val module: AppModule,
) : ProdigiRepository {
    private val api: ProdigiApi = module.api
    private val db: ContentsDatabase = module.db

    override suspend fun getDigitalContents(
        context: Context,
        id: String,
    ): Flow<ApiResult<List<Content>>> {
        return flow {
            emit(ApiResult.Loading())

            val localContent = try {
                db.dao.getContentByContentKey(id)
            } catch (e: Exception) {
                null
            }

            if (localContent != null) {
                Log.i("Prodigi.Repository", "Cache hit")
                val contentResult = Content(
                    id = localContent.contentId,
                    title = localContent.title,
                    collection = Collection(
                        name = localContent.collectionName
                    ),
                    link = Link(
                        targetUrl = localContent.targetLink,
                        url = localContent.contentKey,
                    )
                )
                emit(ApiResult.Success(listOf(contentResult)))
                return@flow
            }

            Log.w("Prodigi.Repository", "Cache miss")
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