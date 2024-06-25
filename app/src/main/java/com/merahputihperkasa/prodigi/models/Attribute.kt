package com.merahputihperkasa.prodigi.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Attribute(
    @SerializedName("key") val key: String,
    @SerializedName("value") val value: String
)