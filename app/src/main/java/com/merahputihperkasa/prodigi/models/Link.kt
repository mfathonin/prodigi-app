package com.merahputihperkasa.prodigi.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Link(
    @SerializedName("targetUrl") val targetUrl: String,
    @SerializedName("url") val url: String
)