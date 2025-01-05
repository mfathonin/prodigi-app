package com.merahputihperkasa.prodigi.repository

import android.content.Context
import android.util.Log
import com.merahputihperkasa.prodigi.AppModule
import com.merahputihperkasa.prodigi.models.BannerItem
import com.merahputihperkasa.prodigi.models.Content
import com.merahputihperkasa.prodigi.models.ContentEntity
import com.merahputihperkasa.prodigi.models.Submission
import com.merahputihperkasa.prodigi.models.SubmissionEntity
import com.merahputihperkasa.prodigi.models.SubmissionResult
import com.merahputihperkasa.prodigi.models.WorkSheet
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
    ): Flow<LoadDataStatus<Content>> = flow {
        emit(LoadDataStatus.Loading())

        // Always emit cached data first if available
        val cachedContent = db.contentsDao.getValidContentByContentKey(System.currentTimeMillis(), id)
        if (cachedContent != null) {
            Log.i("Prodigi.Repository", "[getDigitalContents] Cache hit")
            emit(LoadDataStatus.Success(cachedContent.toContent()))
        }

        // Fetch from network if force refresh or cache miss
        if (cachedContent == null) {
            Log.w("Prodigi.Repository", "[getDigitalContents] Cache miss")
            try {
                val digitalContents = api.getDigitalContents(id, context.packageName)
                val expirationTime = System.currentTimeMillis() + expirationPeriod
                val content = digitalContents.data
                db.contentsDao.upsertContent(content.toContentEntity(expirationTime))

                Log.i("Prodigi.Repository", "[getDigitalContents] Updated: $content")
                emit(LoadDataStatus.Success(digitalContents.data))
            } catch (e: Exception) {
                Log.e("Prodigi.Repository", "[getDigitalContents] Error: ${e.message}")
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
                val updatedContent = result.data.toContentEntity(expirationTime)
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
                val bannerItemEntities = bannerItems.data.map { it.toEntity(expirationTime) }
                db.bannerItemsDao.upsertBannerItems(bannerItemEntities)
                emit(LoadDataStatus.Success(bannerItems.data))
            } catch (e: Exception) {
                emit(LoadDataStatus.Error("Error loading banner items", cachedBannerItems.map { it.toBannerItem() }))
            }
        }
    }

    override suspend fun getWorkSheetConfig(id: String, forceRefresh: Boolean): Flow<LoadDataStatus<WorkSheet>> = flow {
        emit(LoadDataStatus.Loading())

        val currentTime = System.currentTimeMillis()
        val cachedWorkSheetConf = db.workSheetsDao.getWorkSheetByUUID(id, currentTime)
        if (cachedWorkSheetConf != null && !forceRefresh) {
            Log.i("Prodigi.Repository", "[getWorksheet.$id] Cache hit")
            emit(LoadDataStatus.Success(cachedWorkSheetConf.toWorkSheet()))
        }

        if (forceRefresh || cachedWorkSheetConf == null) {
            Log.w("Prodigi.Repository", "[getWorkSheet.$id] Cache miss")
            try {
                val conf = api.getWorksheetConf(id)
                val expirationTime = currentTime + expirationPeriod
                val workSheetsEntity = conf.data.toEntity(expirationTime)
                db.workSheetsDao.upsertWorkSheet(workSheetsEntity)
                emit(LoadDataStatus.Success(conf.data))
            } catch (e: Exception) {
                emit(LoadDataStatus.Error("Error load worksheet conf"))
            }
        }
    }

    override suspend fun saveProfile(
        id: Int?,
        workSheetId: String,
        name: String,
        idNumber: String,
        className: String,
        schoolName: String,
        answers: List<Int>
    ): Int {
        val submission = SubmissionEntity(
            id = id ?: 0,
            name = name,
            idNumber = idNumber,
            className = className,
            schoolName = schoolName,
            worksheetUuid = workSheetId,
            answers = answers
        )

        val submissionId = db.submissionDao.upsertSubmission(submission)
        Log.i("Prodigi.Repository", "[new.submission.$workSheetId] SubmissionID: $submissionId")

        return submissionId.toInt()
    }

    override suspend fun getSubmissionById(id: Int): Flow<LoadDataStatus<Submission>> = flow {
        emit(LoadDataStatus.Loading())

        val submission = db.submissionDao.getSubmissionById(id)
        if (submission == null) {
            emit(LoadDataStatus.Error("Submission not found"))
        } else {
            emit(LoadDataStatus.Success(submission.toSubmission()))
        }
    }

    override suspend fun submitEvaluateAnswer(submissionId: Int, workSheetId: String, submission: Submission): Flow<LoadDataStatus<Submission>> = flow {
        emit(LoadDataStatus.Loading())
        val result: SubmissionResult = api.submitWorksheet(workSheetId, submission.toSubmissionBody())
        Log.i("Prodigi.API", "[submit.answer.$workSheetId] $result")

        if (result.success) {
            emit(LoadDataStatus.Success(result.data))
            val finalSubmissionEntity = result.data
                .copy(answers = submission.answers)
                .toSubmissionEntity(submissionId, workSheetId)
            db.submissionDao.upsertSubmission(finalSubmissionEntity)
            Log.i("Prodigi.Repository", "[submit.answer.$workSheetId] SubmissionID: $submissionId, score: ${finalSubmissionEntity.totalPoints}")
        } else {
            Log.e("Prodigi.Repository", "[submit.answer.$workSheetId] Failed to evaluate answers, $result")
            emit(LoadDataStatus.Error("Failed to evaluate answers", submission))
        }
    }
}