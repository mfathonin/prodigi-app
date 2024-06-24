package com.merahputihperkasa.prodigi.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.merahputihperkasa.prodigi.R
import com.merahputihperkasa.prodigi.ui.theme.ProdigiBookReaderTheme

@Composable
fun CameraView(
    modifier: Modifier = Modifier,
    onInitialized: (codeScanner: CodeScanner) -> Unit,
    onScanned: (String) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var codeScanner by remember {
        mutableStateOf<CodeScanner?>(null)
    }

    Box(modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                CodeScannerView(it).apply {
                    frameCornersRadius = 50
                    frameSize = 0.7f
                    frameVerticalBias = 0.25f

                    val cs = CodeScanner(it, this).apply {
                        isAutoFocusEnabled = true
                        isAutoFocusButtonVisible = false
                        scanMode = ScanMode.CONTINUOUS
                        camera = CodeScanner.CAMERA_BACK
                        decodeCallback = DecodeCallback { result ->
                            onScanned.invoke(result.text)
                            releaseResources()
                        }
                        errorCallback = ErrorCallback { error ->
                            println("[ERROR] Camera error: ${error.message}")
                            releaseResources()
                        }
                    }.also { cs -> codeScanner = cs }
                    cs.startPreview()
                    onInitialized.invoke(cs)

                }
            },
            modifier = Modifier,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .height(64.dp)
                .wrapContentSize(Alignment.TopCenter)
                .align(Alignment.Center)
                .offset(y = 64.dp)
        ) {
            Text(
                text = stringResource(R.string.welcome_instruction),
                textAlign = TextAlign.Center,
                color = Color.White
            )
        }

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
fun CameraViewPreview() {
    ProdigiBookReaderTheme {
        CameraView(
            modifier = Modifier,
            onInitialized = { },
            onScanned = {
                println(it)
            }
        )
    }
}