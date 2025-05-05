package com.merahputihperkasa.prodigi.repository

import android.content.Context
import android.util.Log
import com.merahputihperkasa.prodigi.AppModule
import com.merahputihperkasa.prodigi.models.Answer
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
    private val context: Context,
) : ProdigiRepository {
    private val api: ProdigiApi = module.api
    private val db: ContentsDatabase = module.db
    private val expirationPeriod = 60 * 60 * 1000 // 1 Hour

    override suspend fun getDigitalContents(
        id: String,
    ): Flow<LoadDataStatus<Content>> = flow {
        emit(LoadDataStatus.Loading())

        // Always emit cached data first if available
        val cachedContent = db.contentsDao
            .getValidContentByContentKey(System.currentTimeMillis(), id)
        if (cachedContent != null) {
            Log.i("Prodigi.Repository", "[getDigitalContents] Cache hit")
            emit(LoadDataStatus.Success(cachedContent.toContent()))
        }

        // Fetch from network if force refresh or cache miss
        if (cachedContent == null) {
            Log.w("Prodigi.Repository", "[getDigitalContents] Cache miss")
            try {
                val digitalContents = api.getDigitalContents(id, context.packageName)
                Log.i("Prodigi.API", "[getDigitalContents] $digitalContents")
                val expirationTime = System.currentTimeMillis() + expirationPeriod
                val content = digitalContents.data
                db.contentsDao.upsertContent(content.toContentEntity(expirationTime))
                emit(LoadDataStatus.Success(digitalContents.data))
            } catch (e: Exception) {
                Log.e("Prodigi.Repository", "[getDigitalContents] Error: ${e.message}")
                emit(LoadDataStatus.Error("Error loading content"))
            }
        }
    }

    override suspend fun getFilteredContents(
        filter: String,
        forceRefresh: Boolean,
    ): Flow<LoadDataStatus<List<ContentEntity>>> = flow {
        emit(LoadDataStatus.Loading())

        val localContent = if (filter.isEmpty()) {
            db.contentsDao.getAllContents()
        } else {
            db.contentsDao.getAllContentsWithFilter(filter)
        }
        emit(LoadDataStatus.Success(localContent))
        Log.i(
            "Prodigi.Repository",
            "[getFilteredContents] Load Cache: ${localContent.size} -> $localContent"
        )

        val currentTime = System.currentTimeMillis()
        val expiredContent = localContent.filter {
            it.expirationTime != 0L && it.expirationTime < currentTime
        }

        if (forceRefresh || expiredContent.isNotEmpty()) {
            Log.w("Prodigi.Repository", "[getFilteredContents] Cache expired, updating")
            val packageName = context.packageName

            expiredContent.forEach { content ->
                try {
                    val result = api.getDigitalContents(content.contentKey, packageName)
                    Log.i(
                        "Prodigi.API",
                        "[getDigitalContents.expired.${content.contentKey}] $result"
                    )
                    val expirationTime = System.currentTimeMillis() + expirationPeriod
                    val updatedContent = result.data.toContentEntity(expirationTime)
                    db.contentsDao.upsertContent(updatedContent)
                    Log.i(
                        "Prodigi.Repository",
                        "[getFilteredContents.expired.${content.contentKey}] Updated: ${updatedContent.contentKey}"
                    )
                } catch (e: Exception) {
                    Log.e(
                        "Prodigi.Repository",
                        "[getFilteredContents.expired.${content.contentKey}] Error: ${e.message}"
                    )
                }
            }
        }
    }

    override suspend fun getBannerItems(forceRefresh: Boolean) =
        flow {
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
                    Log.i("Prodigi.API", "[getBannerItems] $bannerItems")
                    val expirationTime = currentTime + expirationPeriod
                    val bannerItemEntities = bannerItems.data.map {
                        it.toEntity(expirationTime)
                    }
                    db.bannerItemsDao.upsertBannerItems(bannerItemEntities)
                    emit(LoadDataStatus.Success(bannerItems.data))
                } catch (e: Exception) {
                    Log.e("Prodigi.Repository", "[getBannerItems] Error: ${e.message}")
                    emit(LoadDataStatus.Error(
                        "Error loading banner items",
                        cachedBannerItems.map { it.toBannerItem() }
                    ))
                }
            }
        }

    override suspend fun getWorkSheetConfig(
        id: String,
        forceRefresh: Boolean,
        byPassInitialLoading: Boolean,
    ): Flow<LoadDataStatus<WorkSheet>> = flow {
        if (!byPassInitialLoading) emit(LoadDataStatus.Loading())

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
                Log.i("Prodigi.API", "[getWorkSheet.$id] $conf")
                val expirationTime = currentTime + expirationPeriod
                val workSheetsEntity = conf.data.toEntity(expirationTime)
                db.workSheetsDao.upsertWorkSheet(workSheetsEntity)
                emit(LoadDataStatus.Success(conf.data))
            } catch (e: Exception) {
                Log.e("Prodigi.Repository", "[getWorkSheet.$id] Error: ${e.message}")
                emit(LoadDataStatus.Error("Error load worksheet conf"))
            }
        }
    }

    override suspend fun upsertSubmission(
        id: Int?,
        workSheetId: String,
        name: String,
        numberId: String,
        className: String,
        schoolName: String,
        answers: List<Answer>,
    ): Int {
        val existingSubmission = if (id != null && id >= 0) {
            db.submissionDao.getSubmissionById(id)
        } else {
            null
        }
        val submission = existingSubmission?.copy(
            name = name,
            numberId = numberId,
            className = className,
            schoolName = schoolName,
            worksheetUuid = workSheetId,
            answers = answers
        ) ?: SubmissionEntity(
            id = id ?: 0,
            name, numberId, className, schoolName,
            worksheetUuid = workSheetId,
            answers = answers
        )

        try {
            val submissionId = db.submissionDao.upsertSubmission(submission)
            Log.i(
                "Prodigi.Repository",
                "[upsertSubmission.$workSheetId] SubmissionID: $submissionId | $submission"
            )

            return submissionId.toInt()
        } catch (e: Exception) {
            Log.e(
                "Prodigi.Repository",
                "[upsertSubmission.$workSheetId] Error saving submission: ${e.message}"
            )
            throw e
        }
    }

    override suspend fun getSubmissionOnWorkSheetId(workSheetId: String): Flow<LoadDataStatus<SubmissionEntity?>> =
        flow {
            emit(LoadDataStatus.Loading())
            try {
                val submissions =
                    db.submissionDao.getSubmissionByWorksheetUuid(workSheetId)
                emit(LoadDataStatus.Success(submissions))
                Log.i(
                    "Prodigi.Repository",
                    "[getSubmissionOnWorkSheetId.$workSheetId] $submissions"
                )
            } catch (e: Exception) {
                Log.e(
                    "Prodigi.Repository",
                    "[getSubmissionOnWorkSheetId.$workSheetId] Error getting submission: ${e.message}"
                )
                emit(LoadDataStatus.Error("Error load submission"))
            }
        }

    override suspend fun getSubmissionById(id: Int): Flow<LoadDataStatus<Submission>> =
        flow {
            emit(LoadDataStatus.Loading())

            val submission = db.submissionDao.getSubmissionById(id)
            if (submission == null) {
                Log.e(
                    "Prodigi.Repository",
                    "[getSubmissionById.$id] Submission not found"
                )
                emit(LoadDataStatus.Error("Submission not found"))
            } else {
                Log.i("Prodigi.Repository", "[getSubmissionById.$id] $submission")
                emit(LoadDataStatus.Success(submission.toSubmission()))
            }
        }

    override suspend fun getSubmissionsHistories(workSheetId: String): Flow<LoadDataStatus<List<SubmissionEntity>>> =
        flow {
            emit(LoadDataStatus.Loading())

            try {
                val submissions = db.submissionDao.getSubmissionHistories(workSheetId)
                emit(LoadDataStatus.Success(submissions))
                Log.i(
                    "Prodigi.Repository",
                    "[getSubmissionsByWorksheetUuid.$workSheetId] $submissions"
                )
            } catch (e: Exception) {
                Log.e(
                    "Prodigi.Repository",
                    "[getSubmissionsByWorksheetUuid.$workSheetId] Error getting submission: ${e.message}"
                )
                emit(LoadDataStatus.Error("Error load submission"))
            }

        }

    override suspend fun submitEvaluateAnswer(
        submissionId: Int,
        workSheetId: String,
        submission: Submission,
    ): Flow<LoadDataStatus<Submission>> = flow {
        emit(LoadDataStatus.Loading())
        val result: SubmissionResult =
            api.submitWorksheet(workSheetId, submission.toSubmissionBody())
        Log.i("Prodigi.API", "[submitEvaluateAnswer.$workSheetId] $result")

        if (result.success) {
            emit(LoadDataStatus.Success(result.data))
            val finalSubmissionEntity = result.data
                .copy(answers = submission.answers)
                .toSubmissionEntity(submissionId, workSheetId)
            db.submissionDao.upsertSubmission(finalSubmissionEntity)
            Log.i(
                "Prodigi.Repository",
                "[submitEvaluateAnswer.$workSheetId] Marked as \"DONE\"; SubmissionID: $submissionId, score: ${finalSubmissionEntity.totalPoints}"
            )
        } else {
            Log.e(
                "Prodigi.Repository",
                "[submitEvaluateAnswer.$workSheetId] Failed to evaluate answers, $result"
            )
            emit(LoadDataStatus.Error("Failed to evaluate answers $result", submission))
        }
    }
}