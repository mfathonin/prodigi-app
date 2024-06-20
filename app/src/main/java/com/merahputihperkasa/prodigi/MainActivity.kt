package com.merahputihperkasa.prodigi

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.budiyev.android.codescanner.CodeScanner
import com.merahputihperkasa.prodigi.ui.components.CameraView
import com.merahputihperkasa.prodigi.ui.components.ResultBottomSheet
import com.merahputihperkasa.prodigi.ui.theme.ProdigiBookReaderTheme

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

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProdigiBookReaderTheme {
                Scaffold(content = { paddingValues ->
                    var result by remember {
                        mutableStateOf<String?>(null)
                    }
                    val bottomSheetState =
                        rememberModalBottomSheetState(skipPartiallyExpanded = false)
                    var codeScanner by remember { mutableStateOf<CodeScanner?>(null) }

                    CameraView(
                        modifier = Modifier.padding(paddingValues),
                        onScanned = { qrResult -> result = qrResult },
                        onInitialized = { scanner -> codeScanner = scanner }
                    )

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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun MainPreview() {
    ProdigiBookReaderTheme {
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        )

        LaunchedEffect(key1 = sheetState) {
            sheetState.show()
        }

        ResultBottomSheet(
            result = "Testing result",
            onDismissRequest = { },
            sheetState = sheetState
        )

    }
}