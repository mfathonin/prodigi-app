package com.merahputihperkasa.prodigi.repository.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.merahputihperkasa.prodigi.models.ContentEntity
import com.merahputihperkasa.prodigi.models.BannerItemEntity

@Database(entities = [ContentEntity::class, BannerItemEntity::class], version = 2)
abstract class ContentsDatabase: RoomDatabase() {
    abstract val contentsDao: ContentsDao
    abstract val bannerItemsDao: BannerItemsDao
}