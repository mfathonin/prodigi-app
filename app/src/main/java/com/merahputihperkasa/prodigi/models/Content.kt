package com.merahputihperkasa.prodigi.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Content(
    @SerializedName("collection") val collection: Collection,
    @SerializedName("collectionId") val collectionId: String? = null,
    @SerializedName("id") val id: String,
    @SerializedName("link") val link: Link,
    @SerializedName("title") val title: String,
)