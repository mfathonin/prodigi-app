package com.merahputihperkasa.prodigi.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contents",
    indices = [Index(value = ["content_key"], unique = true)]
)
data class ContentEntity(
    @PrimaryKey
    @ColumnInfo(name = "content_key")
    val contentKey: String,
    @ColumnInfo(name = "content_id")
    val contentId: String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "type")
    val type: String,
    @ColumnInfo(name = "collection_name")
    val collectionName: String,
    @ColumnInfo(name = "target_link")
    val targetLink: String,
    @ColumnInfo(name = "expiration_time")
    val expirationTime: Long = 0,  // Default to 0 for backward compatibility
    @ColumnInfo(name = "last_fetch_time")
    val lastFetchTime: Long = System.currentTimeMillis(),
)

fun ContentEntity.toContent() = Content(
    id = contentId,
    title = title,
    type = type,
    collection = Collection(name = collectionName),
    link = Link(url = contentKey, targetUrl = targetLink)
)