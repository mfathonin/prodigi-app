package com.merahputihperkasa.prodigi.repository.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.merahputihperkasa.prodigi.models.ContentEntity

@Database(entities = [ContentEntity::class], version = 1)
abstract class ContentsDatabase: RoomDatabase() {
    abstract val dao: ContentsDao
}