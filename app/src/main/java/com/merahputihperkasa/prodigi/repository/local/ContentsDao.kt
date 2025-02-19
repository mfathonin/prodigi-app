package com.merahputihperkasa.prodigi.repository.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.merahputihperkasa.prodigi.models.ContentEntity

@Dao
interface ContentsDao {
    @Upsert
    suspend fun upsertContent(content: ContentEntity)

    @Delete
    suspend fun deleteContent(content: ContentEntity)

    @Query("SELECT * FROM contents")
    suspend fun getAllContents(): List<ContentEntity>

    @Query("SELECT * FROM contents WHERE (title LIKE '%' || :filter || '%' OR collection_name LIKE '%' || :filter || '%' OR content_key LIKE '%' || :filter || '%')")
    suspend fun getAllContentsWithFilter(filter: String): List<ContentEntity>

    @Query("SELECT * FROM contents WHERE content_key = :contentKey AND (expiration_time > :currentTime)")
    suspend fun getValidContentByContentKey(currentTime: Long, contentKey: String): ContentEntity?
}