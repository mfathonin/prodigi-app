package com.merahputihperkasa.prodigi.repository.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration2To3: Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE contents ADD COLUMN type TEXT NOT NULL DEFAULT 'content'
            """
        )
    }
}