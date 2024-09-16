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
import com.merahputihperkasa.prodigi.ui.screens.HistoryScreen
import com.merahputihperkasa.prodigi.ui.screens.QRScanScreen
import androidx.work.*
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
            NavHost(navController, startDestination = QRScanScreen) {
                composable<QRScanScreen> {
                    QRScanScreen(navController)
                }
                composable<HistoryScreen> {
                    HistoryScreen(navController)
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