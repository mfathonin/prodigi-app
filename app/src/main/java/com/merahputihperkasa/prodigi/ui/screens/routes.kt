package com.merahputihperkasa.prodigi.ui.screens

import kotlinx.serialization.Serializable

@Serializable
object QRScan

@Serializable
object History

@Serializable
data class WorksheetDetail(val id: String)

@Serializable
data class WorkSheet(val id: Int, val worksheetId: String)