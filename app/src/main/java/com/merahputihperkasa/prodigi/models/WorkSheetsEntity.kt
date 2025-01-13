package com.merahputihperkasa.prodigi.models

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "worksheets")
data class WorkSheetsEntity (
    @PrimaryKey val uuid: String,
    val id: String,
    @ColumnInfo(name = "book_id")
    val bookId: String,
    val counts: Int,
    val points: List<Int>,
    @ColumnInfo(name = "n_options")
    val options: List<Int>,
    @ColumnInfo(name = "book_title")
    val bookTitle: String? = null,
    @ColumnInfo(name = "content_title")
    val contentTitle: String? = null,


    @ColumnInfo(name = "expiration_time")
    val expirationTime: Long,
    @ColumnInfo(name = "last_fetch_time")
    val lastFetchTime: Long = System.currentTimeMillis(),
) {
    fun toWorkSheet(): WorkSheet {
        return WorkSheet(
            id,
            uuid,
            bookId,
            bookTitle,
            contentTitle,
            counts,
            points,
            options
        )
    }
}

@Keep
data class WorkSheet (
    @SerializedName("id") val id: String,
    @SerializedName("uuid") val uuid: String,
    @SerializedName("book_id") val bookId: String,
    @SerializedName("bookTitle") val bookTitle: String? = null,
    @SerializedName("contentTitle") val contentTitle: String? = null,

    @SerializedName("counts") val counts: Int,
    @SerializedName("points") val points: List<Int>,
    @SerializedName("n_options") val options: List<Int>
) {
    fun toEntity(expirationTime: Long): WorkSheetsEntity {
        return WorkSheetsEntity(
            id = id,
            uuid = uuid,
            bookId = bookId,
            bookTitle = bookTitle,
            contentTitle = contentTitle,
            counts = counts,
            points = points,
            options = options,
            expirationTime = expirationTime
        )
    }
}

@Keep
data class WorkSheetResult (
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: WorkSheet
)

