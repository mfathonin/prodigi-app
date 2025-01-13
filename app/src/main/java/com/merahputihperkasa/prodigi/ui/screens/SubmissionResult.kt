package com.merahputihperkasa.prodigi.ui.screens

import android.Manifest
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.merahputihperkasa.prodigi.MainActivity
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.merahputihperkasa.prodigi.ProdigiApp
import com.merahputihperkasa.prodigi.R
import com.merahputihperkasa.prodigi.models.SubmissionEntity
import com.merahputihperkasa.prodigi.models.WorkSheet
import com.merahputihperkasa.prodigi.repository.LoadDataStatus
import com.merahputihperkasa.prodigi.repository.ProdigiRepositoryImpl
import com.merahputihperkasa.prodigi.ui.theme.ProdigiBookReaderTheme
import com.merahputihperkasa.prodigi.utils.saveToDisk
import com.merahputihperkasa.prodigi.utils.shareBitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import sv.lib.squircleshape.CornerSmoothing
import sv.lib.squircleshape.SquircleShape
import java.io.IOException

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SubmissionResultScreen(
    submissionId: Int,
    workSheetUUID: String,
    submissionEntity: SubmissionEntity?,
    workSheet: WorkSheet?,
) {
    ProdigiBookReaderTheme {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val snackBarHostState = remember { SnackbarHostState() }
        val graphicsLayer = rememberGraphicsLayer()

        val writeStorageAccessState = rememberMultiplePermissionsState(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // No permissions are needed on Android 10+ to add files in the shared storage
                emptyList()
            } else {
                listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        )
        fun shareBitmapFromComposable() {
            if (writeStorageAccessState.allPermissionsGranted) {
                scope.launch {
                    var bitmap: ImageBitmap? = null
                    try {
                        bitmap = graphicsLayer.toImageBitmap()
                        val uri = bitmap.asAndroidBitmap().saveToDisk(context)
                        shareBitmap(context, uri)
                    } catch (e: Exception) {
                        val tag = "Prodigi.SubmissionResult"
                        when (e) {
                            is IOException -> {
                                Log.e(tag, "File operation error: $e")
                            }
                            is SecurityException -> {
                                Log.e(tag, "Permission error: $e")
                            }
                            else -> {
                                Log.e(tag, "Unexpected error: $e")
                            }
                        }
                        Toast.makeText(
                            context,
                            context.getString(
                                R.string.general_error_msg,
                                context.getString(R.string.submission_result_share_error_descriptor)
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                    } finally {
                        bitmap?.asAndroidBitmap()?.recycle()
                    }
                }
            } else if (writeStorageAccessState.shouldShowRationale) {
                scope.launch {

                    val result = snackBarHostState.showSnackbar(
                        message = context.getString(R.string.permission_request_storage_message),
                        actionLabel = context.getString(R.string.permission_request_storage_action_label)
                    )

                    if (result == SnackbarResult.ActionPerformed) {
                        writeStorageAccessState.launchMultiplePermissionRequest()
                    }
                }
            } else {
                writeStorageAccessState.launchMultiplePermissionRequest()
            }
        }

        Scaffold(
            Modifier
                .drawWithCache {
                    onDrawWithContent {
                        graphicsLayer.record {
                            this@onDrawWithContent.drawContent()
                        }
                        drawLayer(graphicsLayer)
                    }
                }
                .background(MaterialTheme.colorScheme.surface),
        ) { paddingValues ->
            val screenHeight = LocalConfiguration.current.screenHeightDp.dp
            val screenWidth = LocalConfiguration.current.screenWidthDp.dp

            val submissionEntityFlow = remember { MutableStateFlow(
                if (submissionEntity != null)
                    LoadDataStatus.Success(submissionEntity.toSubmission())
                else LoadDataStatus.Loading()
            )}
            val worksheetFlow = remember { MutableStateFlow(
                if (workSheet != null) LoadDataStatus.Success(workSheet)
                else LoadDataStatus.Loading()
            )}
            val submissionState = submissionEntityFlow.collectAsState().value
            val worksheetState = worksheetFlow.collectAsState().value
            val submissionData = submissionState.data
            val worksheetData = worksheetState.data

            if (submissionState is LoadDataStatus.Loading || worksheetState is LoadDataStatus.Loading) {
                val repo = ProdigiRepositoryImpl(ProdigiApp.appModule, context)
                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            scope.launch {
                                loadSubmission(submissionId, repo, submissionEntityFlow)
                                loadWorkSheet(workSheetUUID, repo, worksheetFlow)
                            }
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                LoadingState()
                return@Scaffold
            }

            // Decoration
            Box(
                Modifier
                    .size(50.dp)
                    .absoluteOffset(x = screenWidth * .01f, y = screenHeight * .005f)
                    .scale(8.4f)
                    .rotate(13f)
                    .alpha(.15f)
                    .clip(SquircleShape(30.dp, CornerSmoothing.Medium))
                    .background(MaterialTheme.colorScheme.surfaceTint)
            )
            Box(
                Modifier
                    .size(50.dp)
                    .absoluteOffset(x = screenWidth * .02f, y = screenHeight * .005f)
                    .scale(9.5f)
                    .rotate(27f)
                    .alpha(.3f)
                    .clip(SquircleShape(35.dp, CornerSmoothing.High))
                    .background(MaterialTheme.colorScheme.surfaceTint)
            )
            Icon(
                painter = painterResource(R.mipmap.ic_logo),
                contentDescription = "Logo Prodigi",
                tint = MaterialTheme.colorScheme.primary.copy(alpha = .25f),
                modifier = Modifier
                    .absoluteOffset(x = screenWidth * .13f, y = 70.dp)
                    .scale(2.4f)
            )

            // Main
            ConstraintLayout(
                Modifier
                    .padding(paddingValues)
                    .zIndex(20f)
                    .padding(horizontal = min(screenWidth * .1f, 20.dp))
                    .fillMaxSize()
            ) {
                val (container, containerBg, logo, greetings) = createRefs()
                Text(
                    stringResource(R.string.submission_result_greetings),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.constrainAs(greetings) {
                        bottom.linkTo(logo.top, 20.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }

                )

                Box(
                    Modifier
                        .constrainAs(logo) {
                            top.linkTo(container.top, (-30).dp)
                            start.linkTo(container.start)
                            end.linkTo(container.end)
                        }
                        .zIndex(2f)
                        .background(
                            MaterialTheme.colorScheme.primary, MaterialTheme.shapes.large
                        )
                        .border(
                            BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceTint),
                            MaterialTheme.shapes.large
                        )
                        .size(56.dp)
                        .absoluteOffset(0.dp, 0.dp)) {
                    Icon(
                        painter = painterResource(R.mipmap.ic_logo),
                        "Logo",
                        Modifier.padding(7.dp),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                val shape = SquircleShape(50.dp, CornerSmoothing.Medium)
                Box(
                    Modifier
                        .constrainAs(containerBg) {
                            top.linkTo(container.top)
                            bottom.linkTo(container.bottom)
                            start.linkTo(container.start)
                            end.linkTo(container.end)
                        }
                        .height(screenHeight * .75f)
                        .fillMaxWidth()
                        .clip(shape)
                        .alpha(.7f)
                        .blur(40.dp, BlurredEdgeTreatment.Unbounded)
                        .shadow(3.dp, shape)
                        .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)))

                Column(
                    Modifier
                        .constrainAs(container) {
                            top.linkTo(parent.top, screenHeight * .07f)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                        .height(screenHeight * .75f)
                        .fillMaxWidth()
                        .padding(20.dp),
                    Arrangement.SpaceBetween,
                    Alignment.CenterHorizontally
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp), horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "${worksheetData?.bookTitle}", style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "${worksheetData?.contentTitle}",
                            style = MaterialTheme.typography.headlineMedium
                        )

                    }

                    Column(
                        Modifier
                            .size(screenHeight * .25f)
                            .aspectRatio(1f, true)
                            .width(IntrinsicSize.Max)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        Arrangement.Center,
                        Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Point",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            "${submissionData?.totalPoints}",
                            color = Color.White,
                            style = MaterialTheme.typography.displayMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        HorizontalDivider(
                            Modifier
                                .fillMaxWidth(.7f)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        listOf(
                                            Color.White.copy(alpha = .01f),
                                            Color.White,
                                            Color.White.copy(alpha = .1f)
                                        ),
                                    )
                                ),
                            color = Color.Transparent,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${submissionData?.correctAnswers} / ${worksheetData?.counts}",
                            color = Color.White
                        )
                        Text(
                            stringResource(R.string.submission_result_correct_count_label),
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Light
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val profile = submissionData?.profile
                        Text(
                            "${profile?.name}", style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            "${profile?.schoolName} | ${profile?.className} |  ${profile?.numberId}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Button(
                            onClick = {
                                shareBitmapFromComposable()
                            },
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 15.dp),
                            shape = MaterialTheme.shapes.large,
                            colors = ButtonDefaults.textButtonColors()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.share),
                                    "Share Button",
                                    Modifier.size(16.dp),
                                )
                                Text(stringResource(R.string.general_share_button))
                            }
                        }
                        Button(
                            onClick = {
                                (context as MainActivity).finish()
                            },
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 25.dp),
                            shape = MaterialTheme.shapes.large,
                        ) {
                            Text(stringResource(R.string.general_finish_button))
                        }
                    }
                }
            }

            // Another decoration
            Box(
                Modifier
                    .absoluteOffset(x = screenWidth * .96f, y = screenHeight * .93f)
                    .size(40.dp)
                    .scale(8f)
                    .rotate(25f)
                    .clip(SquircleShape(30.dp, CornerSmoothing.Medium))
                    .background(MaterialTheme.colorScheme.surfaceTint.copy(alpha = .15f))
            )
            Box(
                Modifier
                    .absoluteOffset(x = screenWidth * .9f, y = screenHeight * .94f)
                    .size(40.dp)
                    .scale(9f)
                    .rotate(49f)
                    .clip(SquircleShape(22.dp, CornerSmoothing.High))
                    .background(MaterialTheme.colorScheme.surfaceTint.copy(alpha = .3f))
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SheetEvaluationPreview(modifier: Modifier = Modifier) {

    val workSheet =
        WorkSheet("id", "uuid", "bookId", "title", "content", 10, List(10) { 1 }, List(10) { 1 })
    val submissionEntity = SubmissionEntity(
        1, "name", "numberId", "className", "schoolName", List(10) { 0 }, 7, 70, workSheet.uuid
    )

    SubmissionResultScreen(
        0, "id",
        submissionEntity = submissionEntity, workSheet = workSheet
    )
}