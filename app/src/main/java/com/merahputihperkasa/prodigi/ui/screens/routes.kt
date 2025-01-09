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

@Serializable
data class WorkSheetEvaluation(val submissionId: Int, val worksheetId: String)

@Serializable
data class SubmissionHistory(val worksheetUUID: String)