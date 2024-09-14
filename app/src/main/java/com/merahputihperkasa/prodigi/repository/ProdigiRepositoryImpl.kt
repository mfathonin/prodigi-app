package com.merahputihperkasa.prodigi.repository

import android.content.Context
import android.util.Log
import com.merahputihperkasa.prodigi.AppModule
import com.merahputihperkasa.prodigi.models.BannerItem
import com.merahputihperkasa.prodigi.models.Content
import com.merahputihperkasa.prodigi.models.ContentEntity
import com.merahputihperkasa.prodigi.models.toContent
import com.merahputihperkasa.prodigi.repository.local.ContentsDatabase
import com.merahputihperkasa.prodigi.repository.network.ProdigiApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException

class ProdigiRepositoryImpl(
    module: AppModule,
) : ProdigiRepository {
    private val api: ProdigiApi = module.api
    private val db: ContentsDatabase = module.db

    override suspend fun getDigitalContents(
        context: Context,
        id: String,
    ): Flow<LoadDataStatus<List<Content>>> {
        return flow {
            emit(LoadDataStatus.Loading())

            val localContent = try {
                db.dao.getContentByContentKey(id)
            } catch (e: Exception) {
                null
            }

            if (localContent != null) {
                Log.i("Prodigi.Repository", "Cache hit")
                val contentResult = localContent.toContent()
                emit(LoadDataStatus.Success(listOf(contentResult)))
                return@flow
            }

            Log.w("Prodigi.Repository", "Cache miss")
            val packageName = context.packageName
            val digitalContents = try {
                api.getDigitalContents(id, packageName)
            } catch (e: IOException) {
                emit(LoadDataStatus.Error(message = "Error loading content"))
                Log.e("Prodigi.Repository", "IOException: Error loading content", e)
                return@flow
            } catch (e: Exception) {
                Log.e("Prodigi.Repository", "Exception: Error loading content", e)
                emit(LoadDataStatus.Error(message = "Error loading content"))
                return@flow
            }

            Log.d("Prodigi.Repository", "Loaded ${digitalContents.contents.size} item(s)")
            emit(LoadDataStatus.Success(digitalContents.contents))
        }
    }

    override suspend fun getFilteredContents(filter: String): Flow<LoadDataStatus<List<ContentEntity>>> {
        return flow {
            val localContent: List<ContentEntity> = try {
                if (filter == "")
                    db.dao.getContents()
                else {
                    db.dao.getContentsWithFilter(filter)
                }
            } catch (e: Exception) {
                emptyList()
            }

            Log.i(
                "Prodigi.Repository",
                "Loaded data with filter($filter): ${localContent.size} item(s)"
            )
            emit(LoadDataStatus.Success(localContent))

            return@flow
        }
    }

    override suspend fun getBannerItems(): Flow<LoadDataStatus<List<BannerItem>>> {
        return flow {
            emit(LoadDataStatus.Loading())
            try {
                val bannerItems = api.getBannerItems()
                emit(LoadDataStatus.Success(bannerItems))
            } catch (e: IOException) {
                emit(LoadDataStatus.Error(message = "Error loading banner items"))
                Log.e("Prodigi.Repository", "IOException: Error loading banner items", e)
            } catch (e: Exception) {
                Log.e("Prodigi.Repository", "Exception: Error loading banner items", e)
                emit(LoadDataStatus.Error(message = "Error loading banner items"))
            }
        }
    }
}