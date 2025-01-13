package com.merahputihperkasa.prodigi.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.merahputihperkasa.prodigi.ProdigiApp
import com.merahputihperkasa.prodigi.R
import com.merahputihperkasa.prodigi.models.SubmissionEntity
import com.merahputihperkasa.prodigi.models.WorkSheet
import com.merahputihperkasa.prodigi.repository.LoadDataStatus
import com.merahputihperkasa.prodigi.repository.ProdigiRepositoryImpl
import com.merahputihperkasa.prodigi.ui.theme.ProdigiBookReaderTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SubmissionHistoryScreen(
    workSheetUUID: String,
    workSheet: WorkSheet? = null,
    onNavigateToResult: (
        submissionId: Int, submissionEntity: SubmissionEntity, worksheetData: WorkSheet
    ) -> Unit = { _, _, _ -> },
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = ProdigiRepositoryImpl(ProdigiApp.appModule, context)

    val workSheetFlow = remember { MutableStateFlow(
        if (workSheet != null) { LoadDataStatus.Success(workSheet)}
        else { LoadDataStatus.Loading() }
    )}
    val submissionsFlow = remember {
        MutableStateFlow<LoadDataStatus<List<SubmissionEntity>>>(LoadDataStatus.Loading())
    }
    val workSheetState = workSheetFlow.collectAsState()
    val submissionsState = submissionsFlow.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scope.launch {
                    loadWorkSheet(workSheetUUID, repo, workSheetFlow)
                    loadSubmissionHistory(workSheetUUID, submissionsFlow, repo)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    ProdigiBookReaderTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = {
                    Text(
                        stringResource(R.string.submission_history_title),
                        modifier = Modifier.padding(5.dp, 10.dp, 5.dp)
                    )
                })
            }
        ) { paddingValue ->
            Column(
                Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(paddingValue)
                    .fillMaxSize()
            ) {
                CompositionLocalProvider(
                    LocalOverscrollConfiguration provides null
                ) {
                    workSheetState.value.data?.let { workSheetData ->
                        Box(Modifier.padding(20.dp, 10.dp, 20.dp)) {
                            WorkSheetHeader(workSheetData)
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                    when (submissionsState.value) {
                        is LoadDataStatus.Success -> {
                            val submissions = (submissionsState.value as LoadDataStatus.Success).data
                                ?: return@CompositionLocalProvider

                            SubmissionHistoryContent(submissions, onNavigateToResult = { id, submission ->
                                if (workSheetState.value.data == null) return@SubmissionHistoryContent

                                onNavigateToResult(id, submission, workSheetState.value.data!!)
                            })
                        }

                        is LoadDataStatus.Error -> {
                            ErrorState(
                                error = submissionsState.value as LoadDataStatus.Error,
                                errorDescriptor = stringResource(R.string.submission_error_descriptor)
                            )
                        }

                        else ->
                            LoadingState(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun SubmissionHistoryContent(
    submissions: List<SubmissionEntity>,
    onNavigateToResult: (submissionId: Int, submissionEntity: SubmissionEntity) -> Unit,
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = 125.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalItemSpacing = 10.dp,
        contentPadding = PaddingValues(20.dp, 10.dp)
    ) {
        items(submissions, key = { it.id }) { submission ->
            Column (
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .clickable {
                        onNavigateToResult(submission.id, submission)
                    }
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceTint.copy(alpha = .4f)),
                        MaterialTheme.shapes.medium
                    )
                    .shadow(
                        1.dp,
                        MaterialTheme.shapes.medium,
                        spotColor = MaterialTheme.colorScheme.surfaceDim
                    )
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val counts = submission.answers.size
                Box {
                    Column(Modifier.size(100.dp), Arrangement.Center, Alignment.CenterHorizontally) {
                        Text(
                            "${submission.totalPoints}",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        HorizontalDivider(
                            modifier = Modifier
                                .padding(bottom = 4.dp)
                                .fillMaxWidth(.7f)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color.Transparent,
                                            MaterialTheme.colorScheme.primary,
                                            Color.Transparent,
                                        )
                                    )
                                ),
                            color = Color.Transparent,
                        )
                        Text(
                            "${submission.correctAnswers} / $counts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .6f)
                        )
                    }
                    CircularProgressIndicator(
                        progress = {
                            if (counts > 0) {
                                (submission.correctAnswers ?: counts) / counts.toFloat()
                            } else {
                                0f
                            }
                        },
                        modifier = Modifier.size(100.dp),
                        color = MaterialTheme.colorScheme.surfaceTint,
                        strokeWidth = 6.dp
                    )
                }

                Spacer(Modifier.height(16.dp))
                Column(Modifier.fillMaxWidth()){
                    Text(submission.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Normal)
                    Text(
                        stringResource(
                            R.string.submission_card_desc,
                            submission.className,
                            submission.idNumber
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f)
                    )
                }
            }
        }
    }
}

suspend fun loadSubmissionHistory(
    worksheetUUID: String,
    submissions: MutableStateFlow<LoadDataStatus<List<SubmissionEntity>>>,
    repo: ProdigiRepositoryImpl
) {
    repo.getSubmissionsHistories(worksheetUUID).collectLatest { data ->
        submissions.update { data }
        Log.i("Prodigi.SubmissionHistories", "[loadSubmissionHistory] Load Submission History for $worksheetUUID")
    }
}

@Preview(showBackground = true)
@Composable
fun SubmissionHistoryPreview(modifier: Modifier = Modifier) {
    val submissions = listOf(
        SubmissionEntity(
            0, "name", "13", "6A", "School Name",
            List(10) { 1 }, 8, 40,
            "worksheetUUID"
        )
    )

    ProdigiBookReaderTheme {
        SubmissionHistoryContent(
            submissions,
            onNavigateToResult = { _, _ -> }
        )
    }
}