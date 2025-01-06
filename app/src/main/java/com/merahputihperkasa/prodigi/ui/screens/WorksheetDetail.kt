package com.merahputihperkasa.prodigi.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.merahputihperkasa.prodigi.ProdigiApp
import com.merahputihperkasa.prodigi.R
import com.merahputihperkasa.prodigi.models.SubmissionEntity
import com.merahputihperkasa.prodigi.models.WorkSheet
import com.merahputihperkasa.prodigi.repository.LoadDataStatus
import com.merahputihperkasa.prodigi.repository.ProdigiRepositoryImpl
import com.merahputihperkasa.prodigi.ui.components.ProfileForm
import com.merahputihperkasa.prodigi.ui.theme.ProdigiBookReaderTheme
import com.merahputihperkasa.prodigi.utils.rememberImeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Composable
fun WorkSheetDetailScreen(workSheetUUID: String, onNavigateStart: (id: Int, worksheetId: String) -> Unit) {
    ProdigiBookReaderTheme {
        val scrollState = rememberScrollState()
        val imeState = rememberImeState()

        LaunchedEffect(key1 = imeState.value) {
            if (imeState.value) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
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

        val workSheetConf = workSheetConfFlow.collectAsState()
        val submissionEntity = submissionEntityFlow.collectAsState()

        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(key1 = lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    scope.launch {
                        loadWorkSheetByUUID(workSheetUUID, repo, workSheetConfFlow)
                        loadSubmissionByWorkSheetId(workSheetUUID, repo, submissionEntityFlow)
                    }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        Scaffold(
            content = { paddingValues ->
                WorksheetDetailContent(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(horizontal = 20.dp)
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    workSheetConf,
                    submissionEntity
                ) {
                    val workSheet = (workSheetConf.value as LoadDataStatus.Success).data
                    val submission = (submissionEntity.value as LoadDataStatus.Success).data

                    if (workSheet != null) {
                        WorkSheetHeader(workSheet)

                        Spacer(Modifier.height(50.dp))

                        ProfileForm(
                            workSheet.uuid,
                            workSheet.counts,
                            profile = submission?.toSubmission()?.profile,
                            submissionId = submission?.id,
                            answers = submission?.toSubmission()?.answers,
                            onSubmitted = { submissionId ->
                                onNavigateStart.invoke(submissionId, workSheet.uuid)
                            }
                        )
                    } else {
                        EmptyState(
                            dataDescription = stringResource(R.string.error_worksheet_descriptor)
                        )
                    }
                }
            }
        )
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
        Arrangement.Center
    ) {
        Text(
            stringResource(R.string.worksheet_title),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp, bottom = 20.dp),
            textAlign = TextAlign.Center, fontSize = 24.sp, fontWeight = FontWeight.SemiBold
        )

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
                errorDescriptor = stringResource(R.string.error_load_worksheet)
            )
        }

        if (workSheetConf.value is LoadDataStatus.Success && submissionEntity.value is LoadDataStatus.Success) {
            content()
        }
    }
}

@Composable
fun WorkSheetHeader(workSheet: WorkSheet) {
    workSheet.bookTitle?.let { bookTitle ->
        Text(bookTitle, fontSize = 18.sp)
    }
    workSheet.contentTitle?.let { contentTitle ->
        Text(
            contentTitle,
            modifier = Modifier
                .padding(vertical = 4.dp)
                .padding(bottom = 8.dp),
            fontSize = 28.sp, fontWeight = FontWeight.Bold
        )
    }
    Text(stringResource(R.string.worksheet_question_numbers, workSheet.counts))
}

private suspend fun loadWorkSheetByUUID(
    uuid: String,
    repo: ProdigiRepositoryImpl,
    stateFlow: MutableStateFlow<LoadDataStatus<WorkSheet>>
) {
    repo.getWorkSheetConfig(uuid, true).collectLatest { workSheetData ->
        stateFlow.update { workSheetData }
    }
}

private suspend fun loadSubmissionByWorkSheetId(
    worksheetId: String,
    repo: ProdigiRepositoryImpl,
    stateFlow: MutableStateFlow<LoadDataStatus<SubmissionEntity?>>
) {
    repo.getSubmissionOnWorkSheetId(worksheetId).collectLatest { submissionData ->
        stateFlow.update { submissionData }
    }
}