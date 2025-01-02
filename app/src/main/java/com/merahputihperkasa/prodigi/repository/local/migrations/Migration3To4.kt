package com.merahputihperkasa.prodigi.repository.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration3To4: Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
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
            
            CREATE TABLE IF NOT EXISTS `submissions` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, /* Unique submission ID */
                `worksheet_uuid` TEXT NOT NULL, /* Foreign key referencing worksheets table */
                `name` TEXT NOT NULL,
                `id_number` TEXT NOT NULL,
                `class_name` TEXT NOT NULL,
                `school_name` TEXT NOT NULL,
                `answers` TEXT NOT NULL, /* Store answers as JSON array, e.g., "[1,2,3]" */
                `correct_answers` INTEGER, /* Optional: Number of correct answers */
                `total_points` INTEGER /* Optional: Total points earned */
            );
            
            ALTER TABLE `submissions`
            ADD CONSTRAINT `fk_submissions_worksheets`
            FOREIGN KEY (`worksheet_uuid`)
            REFERENCES `worksheets`(`uuid`)
            ON DELETE CASCADE;
            """
        )
    }
}