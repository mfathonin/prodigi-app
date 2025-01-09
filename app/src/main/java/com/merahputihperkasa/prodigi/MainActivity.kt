package com.merahputihperkasa.prodigi

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.merahputihperkasa.prodigi.models.SubmissionEntity
import com.merahputihperkasa.prodigi.ui.screens.History
import com.merahputihperkasa.prodigi.ui.screens.HistoryScreen
import com.merahputihperkasa.prodigi.ui.screens.QRScan
import com.merahputihperkasa.prodigi.ui.screens.QRScanScreen
import com.merahputihperkasa.prodigi.ui.screens.WorkSheetSubmission
import com.merahputihperkasa.prodigi.ui.screens.WorkSheetDetailScreen
import com.merahputihperkasa.prodigi.ui.screens.SubmissionResult
import com.merahputihperkasa.prodigi.ui.screens.SubmissionResultScreen
import com.merahputihperkasa.prodigi.ui.screens.WorkSheetSubmissionScreen
import com.merahputihperkasa.prodigi.ui.screens.WorksheetDetail
import com.merahputihperkasa.prodigi.utils.DataSyncWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onStart() {
        super.onStart()
        val permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
        )

        permissions.forEach {
            if (checkSelfPermission(it) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissions.toTypedArray(), 0)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scheduleDataSync()
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val submissionEntity = remember {
                mutableStateOf<SubmissionEntity?>(null)
            }
            val workSheet = remember {
                mutableStateOf<com.merahputihperkasa.prodigi.models.WorkSheet?>(null)
            }

            NavHost(navController, startDestination = QRScan) {
                composable<QRScan> {
                    QRScanScreen(navController)
                }
                composable<History> {
                    HistoryScreen(navController)
                }
                composable<WorksheetDetail>(
                    deepLinks = listOf(
                        navDeepLink<WorksheetDetail>(
                            basePath = "https://${ProdigiApp.appModule.internalSourceDomain}/quiz"
                        )
                    )
                ) {
                    val workSheetUUID = it.toRoute<WorksheetDetail>().id
                    WorkSheetDetailScreen(
                        workSheetUUID,
                        onNavigateStart = { submissionId, worksheetId, workSheetData ->
                            workSheet.value = workSheetData

                            navController.navigate(WorkSheetSubmission(submissionId, worksheetId))
                        }
                    )
                }
                composable<WorkSheetSubmission> { backStackEntry ->
                    val id = backStackEntry.arguments?.getInt("submissionId") ?: run {
                        Log.e(
                            "Prodigi.WSSubmission",
                            "[parse.arguments] Missing submissionId argument"
                        )
                        return@composable
                    }
                    val worksheetId = backStackEntry.arguments?.getString("worksheetUUID") ?: run {
                        Log.e(
                            "Prodigi.WSSubmission",
                            "[parse.arguments] Missing worksheetUUID argument"
                        )
                        return@composable
                    }

                    val workSheetData = getEntity(workSheet) {
                        it.uuid == worksheetId
                    }

                    WorkSheetSubmissionScreen(
                        id,
                        worksheetId,
                        workSheetData = workSheetData,
                        onEvaluateSuccess = { submissionId, submission ->
                            if (submissionId != null && submission != null) {
                                submissionEntity.value = submission
                                    .toSubmissionEntity(submissionId, worksheetId)

                                navController.navigate(
                                    SubmissionResult(id, worksheetId)
                                )
                            }
                        })
                }
                composable<SubmissionResult> { backStackEntry ->
                    val submissionId = backStackEntry.arguments?.getInt("submissionId") ?: run {
                        Log.e(
                            "Prodigi.WSEvaluation",
                            "[parse.arguments] Missing submissionId argument"
                        )
                        return@composable
                    }
                    val worksheetId = backStackEntry.arguments?.getString("worksheetUUID") ?: run {
                        Log.e(
                            "Prodigi.WSEvaluation",
                            "[parse.arguments] Missing worksheetUUID argument"
                        )
                        return@composable
                    }

                    val submissionEty = getEntity(submissionEntity) {
                        it.id == submissionId && it.worksheetUuid == worksheetId
                    } ?: return@composable

                    val workSt = getEntity(workSheet) {
                        it.uuid == worksheetId
                    } ?: return@composable

                    SubmissionResultScreen(
                        submissionEntity = submissionEty,
                        workSheet = workSt,
                    )
                }
            }
        }
    }

    private fun scheduleDataSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<DataSyncWorker>(1, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "DataSync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    private fun <T> getEntity(
        entity: MutableState<T?>,
        predicate: (T) -> Boolean,
    ): T? {
        return entity.value.takeIf { it != null && predicate(it) }
    }
}