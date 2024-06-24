package com.merahputihperkasa.prodigi.repository.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.merahputihperkasa.prodigi.models.ContentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentsDao {
    @Upsert
    suspend fun addContent(content: ContentEntity)

    @Delete
    suspend fun deleteContent(content: ContentEntity)

    @Query("SELECT * FROM contents")
    suspend fun getContents(): List<ContentEntity>

    @Query("SELECT * FROM contents WHERE title LIKE '%' || :filter || '%' OR collection_name LIKE '%' || :filter || '%' OR content_key LIKE '%' || :filter || '%'")
    suspend fun getContentsWithFilter(filter: String): List<ContentEntity>

    @Query("SELECT * FROM contents WHERE content_key = :contentKey")
    suspend fun getContentByContentKey(contentKey: String): ContentEntity
}