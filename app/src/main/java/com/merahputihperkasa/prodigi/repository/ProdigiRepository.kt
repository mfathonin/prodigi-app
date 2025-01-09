package com.merahputihperkasa.prodigi.repository

import com.merahputihperkasa.prodigi.models.BannerItem
import com.merahputihperkasa.prodigi.models.Content
import com.merahputihperkasa.prodigi.models.ContentEntity
import com.merahputihperkasa.prodigi.models.Submission
import com.merahputihperkasa.prodigi.models.SubmissionEntity
import com.merahputihperkasa.prodigi.models.WorkSheet
import kotlinx.coroutines.flow.Flow

interface ProdigiRepository {
    suspend fun getDigitalContents(id: String): Flow<LoadDataStatus<Content>>
    suspend fun getFilteredContents(filter: String, forceRefresh: Boolean): Flow<LoadDataStatus<List<ContentEntity>>>

    suspend fun getBannerItems(forceRefresh: Boolean): Flow<LoadDataStatus<List<BannerItem>>>

    suspend fun getWorkSheetConfig(id: String, forceRefresh: Boolean, byPassInitialLoading: Boolean = false): Flow<LoadDataStatus<WorkSheet>>

    suspend fun getSubmissionOnWorkSheetId(workSheetId: String): Flow<LoadDataStatus<SubmissionEntity?>>
    suspend fun getSubmissionById(id: Int): Flow<LoadDataStatus<Submission>>
    suspend fun upsertSubmission(id: Int? = 0, workSheetId: String, name: String, idNumber: String, className: String, schoolName: String, answers: List<Int>): Int
    suspend fun submitEvaluateAnswer(submissionId: Int, workSheetId: String, submission: Submission): Flow<LoadDataStatus<Submission>>
}

