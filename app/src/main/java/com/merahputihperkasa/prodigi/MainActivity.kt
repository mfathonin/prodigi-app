package com.merahputihperkasa.prodigi

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.merahputihperkasa.prodigi.ui.screens.History
import com.merahputihperkasa.prodigi.ui.screens.HistoryScreen
import com.merahputihperkasa.prodigi.ui.screens.QRScan
import com.merahputihperkasa.prodigi.ui.screens.QRScanScreen
import com.merahputihperkasa.prodigi.ui.screens.WorkSheet
import com.merahputihperkasa.prodigi.ui.screens.WorkSheetDetailScreen
import com.merahputihperkasa.prodigi.ui.screens.WorkSheetScreen
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissions.forEach {
                if (checkSelfPermission(it) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(permissions.toTypedArray(), 0)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scheduleDataSync()
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
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
                    val id = it.toRoute<WorksheetDetail>().id
                    WorkSheetDetailScreen(id) { submissionId, worksheetId ->
                        navController.navigate(WorkSheet(submissionId, worksheetId))
                    }
                }
                composable<WorkSheet> { backStackEntry ->
                    val id = backStackEntry.arguments?.getInt("id")
                    val worksheetId = backStackEntry.arguments?.getString("worksheetId")
                    if (id != null && worksheetId != null) {
                        WorkSheetScreen(id, worksheetId) {
                            navController.popBackStack<WorksheetDetail>(true)
                        }
                    }
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
}