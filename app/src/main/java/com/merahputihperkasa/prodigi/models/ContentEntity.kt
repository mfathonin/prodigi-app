package com.merahputihperkasa.prodigi.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contents",
    indices = [Index(value = ["content_id"], unique = true)]
)
data class ContentEntity(
    @ColumnInfo(name = "content_id")
    val contentId: String,
    @ColumnInfo(name = "content_key")
    val contentKey: String,

    val title: String,
    @ColumnInfo(name = "collection_name")
    val collectionName: String,
    @ColumnInfo(name = "target_link")
    val targetLink: String,

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
)