package com.merahputihperkasa.prodigi.repository

import android.content.Context
import android.util.Log
import com.merahputihperkasa.prodigi.AppModule
import com.merahputihperkasa.prodigi.models.BannerItem
import com.merahputihperkasa.prodigi.models.Content
import com.merahputihperkasa.prodigi.models.ContentEntity
import com.merahputihperkasa.prodigi.models.toBannerItem
import com.merahputihperkasa.prodigi.models.toContent
import com.merahputihperkasa.prodigi.models.toEntity
import com.merahputihperkasa.prodigi.repository.local.ContentsDatabase
import com.merahputihperkasa.prodigi.repository.network.ProdigiApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ProdigiRepositoryImpl(
    module: AppModule,
    private val context: Context
) : ProdigiRepository {
    private val api: ProdigiApi = module.api
    private val db: ContentsDatabase = module.db
    private val expirationPeriod = 60 * 60 * 1000 // 1 Hour

    override suspend fun getDigitalContents(
        id: String,
    ): Flow<LoadDataStatus<List<Content>>> = flow {
        emit(LoadDataStatus.Loading())

        // Always emit cached data first if available
        val cachedContent = db.contentsDao.getValidContentByContentKey(System.currentTimeMillis(), id)
        if (cachedContent != null) {
            Log.i("Prodigi.Repository", "[getDigitalContents] Cache hit")
            emit(LoadDataStatus.Success(listOf(cachedContent.toContent())))
        }

        // Fetch from network if force refresh or cache miss
        if (cachedContent == null) {
            Log.w("Prodigi.Repository", "[getDigitalContents] Cache miss")
            try {
                val digitalContents = api.getDigitalContents(id, context.packageName)
                val expirationTime = System.currentTimeMillis() + expirationPeriod
                digitalContents.contents.forEach { content ->
                    db.contentsDao.upsertContent(content.toContentEntity(expirationTime))
                }
                emit(LoadDataStatus.Success(digitalContents.contents))
            } catch (e: Exception) {
                emit(LoadDataStatus.Error("Error loading content"))
            }
        }
    }

    override suspend fun getFilteredContents(filter: String, forceRefresh: Boolean): Flow<LoadDataStatus<List<ContentEntity>>> = flow {
        emit(LoadDataStatus.Loading())

        val localContent = if (filter.isEmpty()) {
            db.contentsDao.getAllContents()
        } else {
            db.contentsDao.getAllContentsWithFilter(filter)
        }
        emit(LoadDataStatus.Success(localContent))

        val currentTime = System.currentTimeMillis()
        val expiredContent = localContent.filter {
            it.expirationTime != 0L && it.expirationTime < currentTime
        }

        if (forceRefresh || expiredContent.isNotEmpty()) {
            Log.i("Prodigi.Repository", "[getFilteredContents] Cache expired, updating")
            val packageName = context.packageName

            expiredContent.forEach { content ->
                val result = api.getDigitalContents(content.contentKey, packageName)
                val expirationTime = System.currentTimeMillis() + expirationPeriod
                val updatedContent = result.contents[0].toContentEntity(expirationTime)
                db.contentsDao.upsertContent(updatedContent)
                Log.i("Prodigi.Repository", "[getFilteredContents] Updated: ${updatedContent.contentKey}")
            }
        }
    }

    override suspend fun getBannerItems(forceRefresh: Boolean): Flow<LoadDataStatus<List<BannerItem>>> = flow {
        emit(LoadDataStatus.Loading())

        val currentTime = System.currentTimeMillis()
        val cachedBannerItems = db.bannerItemsDao.getValidBannerItems(currentTime)

        if (cachedBannerItems.isNotEmpty() && !forceRefresh) {
            Log.i("Prodigi.Repository", "[getBannerItems] Cache hit")
            emit(LoadDataStatus.Success(cachedBannerItems.map { it.toBannerItem() }))
        }

        if (forceRefresh || cachedBannerItems.isEmpty()) {
            Log.w("Prodigi.Repository", "[getBannerItems] Cache miss")
            try {
                val bannerItems = api.getBannerItems()
                val expirationTime = currentTime + expirationPeriod
                val bannerItemEntities = bannerItems.map { it.toEntity(expirationTime) }
                db.bannerItemsDao.upsertBannerItems(bannerItemEntities)
                emit(LoadDataStatus.Success(bannerItems))
            } catch (e: Exception) {
                emit(LoadDataStatus.Error("Error loading banner items", cachedBannerItems.map { it.toBannerItem() }))
            }
        }
    }
}