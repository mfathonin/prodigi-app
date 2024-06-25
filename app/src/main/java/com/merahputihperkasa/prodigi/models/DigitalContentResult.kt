package com.merahputihperkasa.prodigi.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class DigitalContentResult(
    @SerializedName("contents") val contents: List<Content>,
)