package com.merahputihperkasa.prodigi.ui.screens

import kotlinx.serialization.Serializable

@Serializable
data class QRScan(val path: String? = null)

@Serializable
object History

@Serializable
data class WorksheetDetail(val id: String)

@Serializable
data class WorkSheetSubmission(val submissionId: Int, val worksheetUUID: String)

@Serializable
data class SubmissionResult(val submissionId: Int, val worksheetUUID: String)

@Serializable
data class SubmissionHistory(val worksheetUUID: String)