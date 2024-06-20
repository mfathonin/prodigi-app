package com.merahputihperkasa.prodigi.models

data class Collection(
    val attributes: List<Attribute>? = null,
    val id: String,
    val name: String,
)