package com.merahputihperkasa.prodigi

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.budiyev.android.codescanner.CodeScanner
import com.merahputihperkasa.prodigi.ui.theme.ProdigiBookReaderTheme

import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode

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
        enableEdgeToEdge()
        setContent {
            ProdigiBookReaderTheme {
                Scaffold(content = { paddingValues ->
                    CameraView(
                        modifier = Modifier.padding(paddingValues),
                        qrReturn = { result ->
                            println(result)
                        }
                    )
                })
            }
        }
    }
}

@Composable
fun CameraView(
    modifier: Modifier = Modifier, qrReturn: (String) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var codeScanner by remember {
        mutableStateOf<CodeScanner?>(null)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
    ) {
        AndroidView(
            factory = {
                CodeScannerView(it).apply {
                    frameCornersRadius = 50

                    val cs = CodeScanner(it, this).apply {
                        isAutoFocusEnabled = true
                        isAutoFocusButtonVisible = false
                        scanMode = ScanMode.CONTINUOUS
                        camera = CodeScanner.CAMERA_BACK
                        decodeCallback = DecodeCallback { result ->
                            qrReturn.invoke(result.text)
                            releaseResources()
                        }
                        errorCallback = ErrorCallback { error ->
                            println("[ERROR] Camera error: ${error.message}")
                            releaseResources()
                        }
                    }.also {cs -> codeScanner = cs }
                    cs.startPreview()
                }
            },
            modifier = Modifier,
        )

        DisposableEffect(key1 = lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_PAUSE) {
                    codeScanner?.releaseResources()
                }
                if (event == Lifecycle.Event.ON_RESUME) {
                    codeScanner?.startPreview()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ProdigiBookReaderTheme {
        CameraView {
            println(it)
        }
    }
}