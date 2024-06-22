package com.merahputihperkasa.prodigi.models

data class Content(
    val collection: Collection,
    val collectionId: String? = null,
    val id: String,
    val link: Link,
    val title: String,
)