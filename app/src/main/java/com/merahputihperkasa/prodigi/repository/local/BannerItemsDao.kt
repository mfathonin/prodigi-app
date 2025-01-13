package com.merahputihperkasa.prodigi.repository.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.merahputihperkasa.prodigi.models.BannerItemEntity

@Dao
interface BannerItemsDao {
    @Upsert
    suspend fun upsertBannerItems(bannerItems: List<BannerItemEntity>)

    @Query("SELECT * FROM banners WHERE expiration_time > :currentTime")
    suspend fun getValidBannerItems(currentTime: Long): List<BannerItemEntity>

    @Query("DELETE FROM banners WHERE expiration_time <= :currentTime")
    suspend fun deleteExpiredBannerItems(currentTime: Long)

}