package com.merahputihperkasa.prodigi.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "banners")
data class BannerItemEntity(
    @PrimaryKey val uuid: String,
    val image: String,
    val url: String,
    @ColumnInfo(name = "expiration_time")
    val expirationTime: Long,
    @ColumnInfo(name = "last_fetch_time")
    val lastFetchTime: Long = System.currentTimeMillis(),
)

fun BannerItem.toEntity(expirationTime: Long): BannerItemEntity {
    return BannerItemEntity(
        uuid = uuid,
        image = image,
        url = url,
        expirationTime = expirationTime
    )
}

fun BannerItemEntity.toBannerItem(): BannerItem {
    return BannerItem(
        uuid = uuid,
        image = image,
        url = url
    )
}