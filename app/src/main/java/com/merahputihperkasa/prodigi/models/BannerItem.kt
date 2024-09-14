package com.merahputihperkasa.prodigi.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class BannerItem(
    @SerializedName("uuid") val uuid: String,
    @SerializedName("image") val image: String,
    @SerializedName("url") val url: String
)