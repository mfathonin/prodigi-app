package com.merahputihperkasa.prodigi.repository.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration3To4 : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create the `worksheets` table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `worksheets` (
                `uuid` TEXT PRIMARY KEY NOT NULL,
                `id` TEXT NOT NULL,
                `book_id` TEXT NOT NULL,
                `book_title` TEXT,
                `content_title` TEXT,
                `counts` INTEGER NOT NULL,
                `points` TEXT NOT NULL,
                `n_options` TEXT NOT NULL,
                `expiration_time` INTEGER NOT NULL,
                `last_fetch_time` INTEGER NOT NULL
            );
            """
        )

        // Create the `submissions` table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `submissions` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `worksheet_uuid` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `id_number` TEXT NOT NULL,
                `class_name` TEXT NOT NULL,
                `school_name` TEXT NOT NULL,
                `answers` TEXT NOT NULL,
                `correct_answers` INTEGER,
                `total_points` INTEGER,
                FOREIGN KEY (`worksheet_uuid`) REFERENCES `worksheets`(`uuid`) 
                    ON DELETE CASCADE
            );
            """
        )

        // Add the expected index on `worksheet_uuid`
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS `index_submissions_worksheet_uuid` 
            ON `submissions` (`worksheet_uuid`);
            """
        )
    }
}
