package com.merahputihperkasa.prodigi.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.merahputihperkasa.prodigi.ProdigiApp
import com.merahputihperkasa.prodigi.R
import com.merahputihperkasa.prodigi.models.Answer
import com.merahputihperkasa.prodigi.models.Profile
import com.merahputihperkasa.prodigi.models.SubmissionEntity
import com.merahputihperkasa.prodigi.models.WorkSheet
import com.merahputihperkasa.prodigi.repository.LoadDataStatus
import com.merahputihperkasa.prodigi.repository.ProdigiRepositoryImpl
import com.merahputihperkasa.prodigi.ui.components.ProfileForm
import com.merahputihperkasa.prodigi.ui.theme.ProdigiBookReaderTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import sv.lib.squircleshape.CornerSmoothing
import sv.lib.squircleshape.SquircleShape

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkSheetDetailScreen(
    workSheetUUID: String,
    onNavigateStart: (id: Int, worksheetId: String, workSheet: WorkSheet) -> Unit,
    onNavigateToHistories: (worksheetData: WorkSheet) -> Unit,
) {
    ProdigiBookReaderTheme {
        val scrollState = rememberScrollState()
        val keyboardHeight = WindowInsets.ime.getBottom(LocalDensity.current)

        LaunchedEffect(key1 = keyboardHeight) {
            Log.i(
                "Prodigi.Worksheet",
                "[scroll.to.end] $keyboardHeight ${scrollState.maxValue}"
            )
            scrollState.animateScrollBy(keyboardHeight.toFloat() * 3f / 5)
        }

        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val repo = ProdigiRepositoryImpl(ProdigiApp.appModule, context)

        val workSheetConfFlow = remember {
            MutableStateFlow<LoadDataStatus<WorkSheet>>(LoadDataStatus.Loading())
        }
        val submissionEntityFlow = remember {
            MutableStateFlow<LoadDataStatus<SubmissionEntity?>>(LoadDataStatus.Loading())
        }
        val submissionHistoriesFlow = remember {
            MutableStateFlow<LoadDataStatus<List<SubmissionEntity>>>(LoadDataStatus.Loading())
        }

        val workSheetConf = workSheetConfFlow.collectAsState()
        val submissionEntity = submissionEntityFlow.collectAsState()
        val submissionCount: Int =
            submissionHistoriesFlow.collectAsState().value.data?.size ?: 0

        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(key1 = lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    scope.launch {
                        loadWorkSheetByUUID(workSheetUUID, repo, workSheetConfFlow)
                        loadSubmissionByWorkSheetId(
                            workSheetUUID,
                            repo,
                            submissionEntityFlow
                        )
                        loadSubmissionHistory(
                            workSheetUUID,
                            submissionHistoriesFlow,
                            repo
                        )
                    }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        Scaffold(
            Modifier
                .fillMaxSize()
                .imePadding()

        ) { paddingValue ->
            CompositionLocalProvider(
                LocalOverscrollConfiguration provides null
            ) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .zIndex(2f)
                        .padding(paddingValue)
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp),
                ) {
                    WorksheetDetailContent(
                        modifier = Modifier.fillMaxHeight(),
                        workSheetConf,
                        submissionEntity
                    ) {
                        val workSheet =
                            (workSheetConf.value as LoadDataStatus.Success).data
                        val submission =
                            (submissionEntity.value as LoadDataStatus.Success).data

                        if (workSheet != null) {
                            WorkSheetHeader(workSheet)

                            Spacer(Modifier.height(20.dp))

                            ProfileForm(
                                submission = submission,
                                submissionCount = submissionCount,
                                onNavigateToHistories = {
                                    onNavigateToHistories.invoke(workSheet)
                                },
                                onSubmitted = { profile ->
                                    scope.launch {
                                        handleSubmitProfile(
                                            repo, workSheet, profile, submission
                                        ) { id ->
                                            onNavigateStart.invoke(
                                                id,
                                                workSheet.uuid,
                                                workSheet
                                            )
                                        }
                                    }
                                },
                            )
                        } else {
                            EmptyState(
                                emptyDataDescription = stringResource(R.string.worksheet_error_descriptor)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WorksheetDetailContent(
    modifier: Modifier = Modifier,
    workSheetConf: State<LoadDataStatus<WorkSheet>>,
    submissionEntity: State<LoadDataStatus<SubmissionEntity?>>,
    content: @Composable () -> Unit,
) {
    Column(
        modifier,
    ) {
        // Sections: App Bar
        Spacer(Modifier.height(20.dp))
        Row {
            val icSize = 44.dp
            Column(
                Modifier
                    .size(icSize)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.shapes.large
                    )
                    .shadow(
                        2.dp,
                        MaterialTheme.shapes.large,
                        spotColor = MaterialTheme.colorScheme.surfaceDim
                    ),
                Arrangement.Center,
                Alignment.CenterHorizontally
            ) {
                Icon(
                    painterResource(R.mipmap.ic_logo),
                    "Logo Prodigi",
                    Modifier.size(28.dp),
                    MaterialTheme.colorScheme.onPrimary
                )
            }

            Text(
                stringResource(R.string.worksheet_title),
                modifier = Modifier
                    .zIndex(2f)
                    .weight(1f)
                    .padding(top = 10.dp, bottom = 20.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Light
            )

            Box(Modifier.requiredSize(icSize))
        }
        Spacer(Modifier.height(30.dp))

        if (workSheetConf.value is LoadDataStatus.Loading || submissionEntity.value is LoadDataStatus.Loading) {
            LoadingState()
        }
        if (workSheetConf.value is LoadDataStatus.Error || submissionEntity.value is LoadDataStatus.Error) {
            val error = if (workSheetConf.value is LoadDataStatus.Error) {
                (workSheetConf.value as LoadDataStatus.Error)
            } else {
                (submissionEntity.value as LoadDataStatus.Error)
            }
            ErrorState(
                error = error,
                errorDescriptor = stringResource(R.string.worksheet_error_load)
            )
        }

        if (workSheetConf.value is LoadDataStatus.Success && submissionEntity.value is LoadDataStatus.Success) {
            content()
        }
    }
}

@Composable
fun WorkSheetHeader(workSheet: WorkSheet) {
    val borderContainer = SquircleShape(28.dp, CornerSmoothing.Medium)
    val border =
        BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceTint.copy(alpha = .4f))

    Column(
        Modifier
            .border(border, borderContainer)
            .shadow(
                2.dp,
                borderContainer,
                spotColor = MaterialTheme.colorScheme.surfaceDim
            )
            .background(MaterialTheme.colorScheme.primary, borderContainer)
            .padding(vertical = 10.dp, horizontal = 15.dp)
    ) {
        Row {
            Column(Modifier.weight(1f)) {
                workSheet.bookTitle?.let { bookTitle ->
                    Text(
                        bookTitle,
                        modifier = Modifier.fillMaxWidth(),
                        fontWeight = FontWeight.W400,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .7f)
                    )
                }
                workSheet.contentTitle?.let { contentTitle ->
                    Text(
                        contentTitle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 15.dp),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .9f),
                        fontWeight = FontWeight.Light
                    )
                }
            }
            Box(
                Modifier
                    .padding(5.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = .35f),
                        MaterialTheme.shapes.large
                    )
                    .padding(10.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.puzzle),
                    contentDescription = "QuizIcon",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        Row(
            Modifier
                .padding(bottom = 5.dp)
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = .35f),
                    MaterialTheme.shapes.large
                )
                .padding(vertical = 4.dp, horizontal = 12.dp)
                .padding(end = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            val color = MaterialTheme.colorScheme.onPrimary
            Box(Modifier.size(15.dp)) {
                Icon(
                    painter = painterResource(R.drawable.message_quote),
                    contentDescription = "Question Number",
                    tint = color
                )
            }
            Text("•", color = color)
            Text(
                stringResource(R.string.worksheet_question_numbers, workSheet.counts),
                fontWeight = FontWeight.Light,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = color
                )
            )
        }
    }
}

suspend fun handleSubmitProfile(
    repo: ProdigiRepositoryImpl,
    workSheet: WorkSheet,
    profile: Profile,
    submissionEntity: SubmissionEntity? = null,
    onSubmitted: (id: Int) -> Unit = {},
) {
    val workSheetId = workSheet.uuid
    val count = workSheet.counts
    val submission = submissionEntity?.toSubmission()
    val answers = submission?.answers

    try {
        // Save data to Room database
        val id = repo.upsertSubmission(
            id = submissionEntity?.id, workSheetId,
            profile.name, profile.numberId, profile.className, profile.schoolName,
            answers = answers ?: List(count) { Answer.None }
        )

        // Navigate to the next screen
        onSubmitted.invoke(submissionEntity?.id ?: id)
    } catch (e: Exception) {
        Log.e(
            "Prodigi.Profile",
            "[handleSubmitProfile] error on upsert submission $e, $submission"
        )
    }
}

private suspend fun loadWorkSheetByUUID(
    uuid: String,
    repo: ProdigiRepositoryImpl,
    stateFlow: MutableStateFlow<LoadDataStatus<WorkSheet>>,
) {
    repo.getWorkSheetConfig(uuid, true).collectLatest { workSheetData ->
        stateFlow.update { workSheetData }
    }
}

private suspend fun loadSubmissionByWorkSheetId(
    worksheetId: String,
    repo: ProdigiRepositoryImpl,
    stateFlow: MutableStateFlow<LoadDataStatus<SubmissionEntity?>>,
) {
    repo.getSubmissionOnWorkSheetId(worksheetId).collectLatest { submissionData ->
        stateFlow.update { submissionData }
    }
}

@Preview(showBackground = true)
@Composable
fun WorksheetDetailPreview(modifier: Modifier = Modifier) {
    val worksheet = remember {
        mutableStateOf(
            LoadDataStatus.Success(WorkSheet(
                "id", "uuid", "bookId", "Book title", "content Title",
                10, List(10) { 0 }, List(10) { 0 }, List(10) { 0 }
            ))
        )
    }
    val submissionEntity = remember {
        mutableStateOf<LoadDataStatus<SubmissionEntity?>>(
            LoadDataStatus.Success(null)
        )
    }

    Scaffold { paddingValue ->
        Column(
            Modifier
                .background(MaterialTheme.colorScheme.surface)
                .fillMaxWidth()
                .padding(paddingValue)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            WorksheetDetailContent(
                modifier = modifier,
                workSheetConf = worksheet,
                submissionEntity = submissionEntity
            ) {
                val workSheet = (worksheet.value).data

                if (workSheet != null) {
                    WorkSheetHeader(workSheet)

                    Spacer(Modifier.height(50.dp))

                    Box(
                        Modifier
                            .fillMaxWidth()
                            .requiredHeight(300.dp)
                            .background(MaterialTheme.colorScheme.error)
                    )
                }
            }
        }
    }
}