package com.merahputihperkasa.prodigi

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.budiyev.android.codescanner.CodeScanner
import com.merahputihperkasa.prodigi.ui.theme.ProdigiBookReaderTheme

import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProdigiBookReaderTheme {
                Scaffold(
                    content = { paddingValues ->
                        CameraView(
                            modifier = Modifier.padding(paddingValues),
                            qrReturn = { result ->
                                println(result)
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun CameraView(
    modifier: Modifier = Modifier,
    qrReturn: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
    ) {
        AndroidView(
            factory = {
                CodeScannerView(it).apply {
                    frameCornersRadius = 50

                    val codeScanner = CodeScanner(it, this).apply {
                        isAutoFocusEnabled = true
                        isAutoFocusButtonVisible = false
                        scanMode = ScanMode.CONTINUOUS
                        camera = CodeScanner.CAMERA_BACK
                        decodeCallback = DecodeCallback { result ->
                            qrReturn.invoke(result.text)
                            releaseResources()
                        }
                        errorCallback = ErrorCallback {
                            println("Camera error: ${it.message}")
                            releaseResources()
                        }
                    }
                    codeScanner.startPreview()
                }
            },
            modifier = Modifier,
        )
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