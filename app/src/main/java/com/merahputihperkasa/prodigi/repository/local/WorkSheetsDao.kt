package com.merahputihperkasa.prodigi.repository.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.merahputihperkasa.prodigi.models.WorkSheetsEntity

@Dao
interface WorkSheetsDao {
    @Upsert
    suspend fun upsertWorkSheet(workSheet: WorkSheetsEntity)

    @Query("SELECT * FROM worksheets WHERE uuid = :uuid AND (expiration_time > :currentTime)")
    suspend fun getWorkSheetByUUID(uuid: String, currentTime: Long): WorkSheetsEntity?
}