package com.merahputihperkasa.prodigi.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Collection(
    @SerializedName("attributes") val attributes: List<Attribute>? = null,
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String,
)