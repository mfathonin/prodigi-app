package com.merahputihperkasa.prodigi.repository.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration4To5 : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add modes column to worksheets
        db.execSQL(
            """
            ALTER TABLE `worksheets`
            ADD COLUMN `modes` TEXT NOT NULL DEFAULT '-1';
            """
        )
        
        // Update modes column to have zeros based on counts using recursive CTE
        db.execSQL(
            """
            WITH RECURSIVE
            zeros(n, str) AS (
                SELECT 1, '0' 
                UNION ALL
                SELECT n + 1, str || ',0'
                FROM zeros 
                WHERE n < (SELECT MAX(counts) FROM worksheets)
            )
            UPDATE worksheets
            SET modes = (
                SELECT str 
                FROM zeros 
                WHERE n = counts
            );
            """
        )

        // convert coma separated string submissions.answers to array formated so it can use gson for converter
        // adding "[" and "]" to the string
        db.execSQL(
            """
            UPDATE submissions
            SET answers = '[' || answers || ']'
            WHERE answers NOT LIKE '[%' AND answers NOT LIKE '%]';
            """
        )

    }
}
