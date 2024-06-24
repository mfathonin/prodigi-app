package com.merahputihperkasa.prodigi.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.budiyev.android.codescanner.CodeScanner
import com.merahputihperkasa.prodigi.ui.components.CameraView
import com.merahputihperkasa.prodigi.ui.components.ResultBottomSheet
import com.merahputihperkasa.prodigi.ui.theme.OnPrimary
import com.merahputihperkasa.prodigi.ui.theme.ProdigiBookReaderTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScanScreen(navController: NavController) {
    var result by remember {
        mutableStateOf<String?>(null)
    }
    val bottomSheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var codeScanner by remember { mutableStateOf<CodeScanner?>(null) }

    ProdigiBookReaderTheme {
        Scaffold(content = { paddingValues ->

            CameraView(
                modifier = Modifier.padding(paddingValues),
                onScanned = { qrResult -> result = qrResult },
                onInitialized = { scanner -> codeScanner = scanner }
            )

            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                IconButton(onClick = {
                    navController.navigate(HistoryScreen)
                }) {
                    Icon(
                        Icons.AutoMirrored.Filled.List,
                        contentDescription = "Riwayat",
                        tint = OnPrimary
                    )
                }
            }

            when {
                result !== null -> {
                    ResultBottomSheet(
                        result = result!!,
                        onDismissRequest = {
                            result = null
                            if (codeScanner != null && !codeScanner!!.isPreviewActive) {
                                codeScanner?.startPreview()
                            }
                        },
                        sheetState = bottomSheetState
                    )
                }
            }
        })
    }
}