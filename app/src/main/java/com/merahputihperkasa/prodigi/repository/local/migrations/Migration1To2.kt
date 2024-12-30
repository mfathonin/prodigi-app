package com.merahputihperkasa.prodigi.repository.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration1To2 : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `banners` (
                `uuid` TEXT PRIMARY KEY NOT NULL,
                `image` TEXT,
                `url` TEXT,
                `expiration_time` INTEGER NOT NULL,
                `last_fetch_time` INTEGER NOT NULL
            )
            """
        )
    }
}
