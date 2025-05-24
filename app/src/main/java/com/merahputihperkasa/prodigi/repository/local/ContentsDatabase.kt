package com.merahputihperkasa.prodigi.repository.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.merahputihperkasa.prodigi.models.AnswerListConverter
import com.merahputihperkasa.prodigi.models.BannerItemEntity
import com.merahputihperkasa.prodigi.models.ContentEntity
import com.merahputihperkasa.prodigi.models.SubmissionEntity
import com.merahputihperkasa.prodigi.models.WorkSheetsEntity
import com.merahputihperkasa.prodigi.utils.IntListConverter

@Database(
    entities = [
        ContentEntity::class,
        BannerItemEntity::class,
        WorkSheetsEntity::class,
        SubmissionEntity::class
    ], version = 5
)
@TypeConverters(IntListConverter::class, AnswerListConverter::class)
abstract class ContentsDatabase : RoomDatabase() {
    abstract val contentsDao: ContentsDao
    abstract val bannerItemsDao: BannerItemsDao
    abstract val workSheetsDao: WorkSheetsDao
    abstract val submissionDao: SubmissionDao
}