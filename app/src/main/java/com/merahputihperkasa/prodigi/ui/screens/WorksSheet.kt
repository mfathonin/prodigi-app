package com.merahputihperkasa.prodigi.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.merahputihperkasa.prodigi.ProdigiApp
import com.merahputihperkasa.prodigi.R
import com.merahputihperkasa.prodigi.models.Profile
import com.merahputihperkasa.prodigi.models.Submission
import com.merahputihperkasa.prodigi.models.WorkSheet
import com.merahputihperkasa.prodigi.repository.LoadDataStatus
import com.merahputihperkasa.prodigi.repository.ProdigiRepositoryImpl
import com.merahputihperkasa.prodigi.ui.components.OptionsCard
import com.merahputihperkasa.prodigi.ui.theme.ProdigiBookReaderTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Composable
fun WorkSheetScreen(id: Int, workSheetId: String) {
    val context = LocalContext.current
    val repo = ProdigiRepositoryImpl(ProdigiApp.appModule, context)

    val workSheetFlow = remember {
        MutableStateFlow<LoadDataStatus<WorkSheet>>(LoadDataStatus.Loading())
    }
    val submissionFlow = remember {
        MutableStateFlow<LoadDataStatus<Submission>>(LoadDataStatus.Loading())
    }

    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scope.launch {
                    loadWorkSheet(workSheetId, repo, workSheetFlow)
                    loadSubmission(id, repo, submissionFlow)
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
            content = { paddingValues ->
                WorkSheetScreenContent(
                    workSheetFlow,
                    submissionFlow,
                    modifier = Modifier
                        .padding(paddingValues),
                    onSave =  { answers ->
                        scope.launch {
                            saveAnswers(
                                submissionId = id,
                                workSheetFlow.value.data!!,
                                submissionFlow.value.data!!,
                                answers,
                                repo
                            )
                        }
                    },
                    onSubmit = { answers ->
                        Log.i("Prodigi.Worksheet", "submit.answers: $answers")
                        scope.launch {
                            submitAnswer(
                                id,
                                workSheetFlow.value.data!!,
                                submissionFlow.value.data!!,
                                answers,
                                repo
                            )
                        }
                    }
                )
            }
        )
    }
}

suspend fun loadWorkSheet(
    workSheetId: String,
    repo: ProdigiRepositoryImpl,
    stateFlow: MutableStateFlow<LoadDataStatus<WorkSheet>>
) {
    repo.getWorkSheetConfig(workSheetId, false).collect { conf ->
        Log.i("Prodigi.Repository", "[load.workSheet.$workSheetId] ${conf.data}")
        stateFlow.value = conf
    }
}

suspend fun loadSubmission(
    id: Int,
    repo: ProdigiRepositoryImpl,
    stateFlow: MutableStateFlow<LoadDataStatus<Submission>>
) {
    val submissionId = "$id".toInt(10)
    repo.getSubmissionById(submissionId).collect { submission ->
        Log.i("Prodigi.Repository", "[load.submission.$submissionId] ${submission.data}")
        stateFlow.value = submission
    }
}

suspend fun saveAnswers(
    submissionId: Int?,
    workSheet: WorkSheet,
    submission: Submission,
    answers: List<Int>,
    repo: ProdigiRepositoryImpl
) {
    // cap the answer to worksheet.counts
    val cleanAnswers = answers.take(workSheet.counts)
    repo.saveProfile(
        id = submissionId,
        workSheet.uuid,
        submission.profile.name,
        submission.profile.idNumber,
        submission.profile.className,
        submission.profile.schoolName,
        cleanAnswers
    )
}

suspend fun submitAnswer(
    submissionId: Int,
    workSheet: WorkSheet,
    submission: Submission,
    answers: List<Int>,
    repo: ProdigiRepositoryImpl
) {
    // cap the answer to worksheet.counts
    val cleanAnswers = answers.take(workSheet.counts)
    repo.submitEvaluateAnswer(
        submissionId,
        workSheet.uuid,
        submission.copy(
            answers = cleanAnswers
        )
    ).collect { res ->
        Log.i("Prodigi.Repository", "[evaluate.answer.$submissionId] $res")
    }
}

@Composable
fun WorkSheetScreenContent(
    workSheetFlow: MutableStateFlow<LoadDataStatus<WorkSheet>> = MutableStateFlow(LoadDataStatus.Loading()),
    submissionFlow: MutableStateFlow<LoadDataStatus<Submission>> = MutableStateFlow(LoadDataStatus.Loading()),
    modifier: Modifier = Modifier,
    onSave: (answers: List<Int>) -> Unit = {},
    onSubmit: (answers: List<Int>) -> Unit = {}
) {
    val workSheet = workSheetFlow.collectAsState()
    val submission = submissionFlow.collectAsState()

    val count by remember { derivedStateOf { workSheet.value.data?.counts } }
    val options by remember { derivedStateOf { workSheet.value.data?.options } }
    val answers = remember { mutableStateOf(submission.value.data?.answers.let { answers ->
        answers ?: List(0) { -1 }
    }) }

    ConstraintLayout(
        modifier
            .padding(horizontal = 20.dp)
            .fillMaxSize(),
    ) {
        val (header, optionsListSection, submitButton) = createRefs()

        if (workSheet.value is LoadDataStatus.Success && submission.value is LoadDataStatus.Success) {
            if (answers.value.isEmpty() && count != null) {
                answers.value = List(count!!) { -1 }
            }

            val conf = workSheet.value.data

            Column(Modifier
                .constrainAs(header) {
                    top.linkTo(parent.top)
                }
                .zIndex(2f)
                .padding(bottom = 20.dp)
                .fillMaxWidth()
            ) {
                Spacer(Modifier.height(30.dp))
                conf?.bookTitle?.let { bookTitle ->
                    Text(bookTitle, fontSize = 16.sp)
                }
                conf?.contentTitle?.let { contentTitle ->
                    Text(
                        contentTitle,
                        modifier = Modifier.padding(vertical = 4.dp),
                        fontSize = 24.sp
                    )
                }
            }

            options?.let { optionsItems ->
                Column(
                    Modifier
                        .constrainAs(optionsListSection) {
                            top.linkTo(header.bottom)
                            bottom.linkTo(submitButton.top)
                            height = Dimension.fillToConstraints
                        }
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                ) {
                    for (index in optionsItems.indices) {
                        OptionsCard(
                            answers = answers,
                            option = optionsItems[index],
                            index = index,
                        )
                        if (index != optionsItems.size - 1) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp, bottom = 5.dp)
                                    .height(1.dp)
                                    .border(
                                        BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                                .copy(alpha = .1f)
                                        )
                                    )
                            )
                        } else {
                            Box(Modifier.padding(top = 10.dp, bottom = 30.dp))
                        }
                    }
                }
            }

            val isFinished = answers.value.filter { it != -1 }.size == count
            val saveButtonColor = if (isFinished) {
                ButtonDefaults.buttonColors()
            } else {
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = {
                    if (isFinished) {
                        onSubmit.invoke(answers.value)
                    } else {
                        onSave.invoke(answers.value)
                    }
                },
                colors = saveButtonColor,
                modifier = Modifier
                    .constrainAs(submitButton) {
                        bottom.linkTo(parent.bottom)
                    }
                    .zIndex(2f)
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                val label = if (isFinished) {
                    stringResource(R.string.worksheet_button_finish) } else {
                    stringResource(R.string.worksheet_button_save) }
                Text(label)
            }
        }

        // Loading State
        if (workSheet.value is LoadDataStatus.Loading || submission.value is LoadDataStatus.Loading) {
            LoadingState()
        }

        // Error worksheet state
        if (workSheet.value is LoadDataStatus.Error) {
            val error = workSheet.value as LoadDataStatus.Error
            ErrorState(
                error = error,
                errorDescriptor = "Error loading worksheet"
            )
        }

        // Error submission state
        if (submission.value is LoadDataStatus.Error) {
            val error = submission.value as LoadDataStatus.Error
            ErrorState(
                error = error,
                errorDescriptor = "Error loading submission"
            )
        }

        createVerticalChain(
            header,
            optionsListSection,
            submitButton,
            chainStyle = ChainStyle.Packed
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WorkSheetScreenContentPreview() {
    WorkSheetScreenContent(
        workSheetFlow = MutableStateFlow(LoadDataStatus.Success(
            WorkSheet(
                id = "worksheet-id",
                uuid = "worksheet-uuid",
                bookId = "book-uuid",
                bookTitle = "Sample Book Title",
                contentTitle = "Sample Content Title",
                counts = 10,
                options = listOf(4,4,3,5,2,4,4,4,4,4),
                points = List(10) { 5 }
            )
        )),
        submissionFlow = MutableStateFlow(LoadDataStatus.Success(
            Submission(
                profile = Profile("name", "13", "claasName", "SchoolName"),
                answers = List(10) { -1 }
            )
        ))
    )
}