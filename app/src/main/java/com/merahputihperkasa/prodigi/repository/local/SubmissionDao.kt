package com.merahputihperkasa.prodigi.repository.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.merahputihperkasa.prodigi.models.SubmissionEntity

@Dao
interface SubmissionDao {
    @Upsert
    suspend fun upsertSubmission(submission: SubmissionEntity): Long

    @Query("SELECT * FROM submissions WHERE worksheet_uuid = :worksheetUuid")
    suspend fun getSubmissionsByWorksheetUuid(worksheetUuid: String): List<SubmissionEntity>

    @Query("SELECT * FROM submissions WHERE id = :id LIMIT 1")
    suspend fun getSubmissionById(id: Int): SubmissionEntity?
}