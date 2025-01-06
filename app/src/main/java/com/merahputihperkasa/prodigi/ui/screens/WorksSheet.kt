package com.merahputihperkasa.prodigi.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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
import com.merahputihperkasa.prodigi.MainActivity
import com.merahputihperkasa.prodigi.ProdigiApp
import com.merahputihperkasa.prodigi.R
import com.merahputihperkasa.prodigi.models.Profile
import com.merahputihperkasa.prodigi.models.Submission
import com.merahputihperkasa.prodigi.models.WorkSheet
import com.merahputihperkasa.prodigi.repository.LoadDataStatus
import com.merahputihperkasa.prodigi.repository.ProdigiRepositoryImpl
import com.merahputihperkasa.prodigi.ui.components.OptionsCard
import com.merahputihperkasa.prodigi.ui.components.SubmitConfimationDialog
import com.merahputihperkasa.prodigi.ui.theme.ProdigiBookReaderTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Composable
fun WorkSheetScreen(id: Int, workSheetId: String, onEvaluateSuccess: () -> Unit = {}) {
    val context = LocalContext.current
    val repo = ProdigiRepositoryImpl(ProdigiApp.appModule, context)

    val workSheetFlow = remember {
        MutableStateFlow<LoadDataStatus<WorkSheet>>(LoadDataStatus.Loading())
    }
    val submissionFlow = remember {
        MutableStateFlow<LoadDataStatus<Submission>>(LoadDataStatus.Loading())
    }
    val workSheet = workSheetFlow.collectAsState()
    val submission = submissionFlow.collectAsState()

    val isLoadingOnSave = remember { mutableStateOf(false) }

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
                // Loading overlay with opacity and blur
                if (isLoadingOnSave.value) {
                    Column(
                        modifier = Modifier
                            .zIndex(10f)
                            .blur(30.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant
                                    .copy(alpha = .6f)
                            )
                            .padding(paddingValues)
                            .clickable(enabled = false) {}
                            .fillMaxSize(),
                        Arrangement.Center,
                        Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Text(
                            stringResource(R.string.submission_saving_label),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
                val lastBackPressTime = remember { mutableLongStateOf(0L) }
                BackHandler {
                    Log.i("Prodigi.Worksheet", "[back.pressed]")
                    if (lastBackPressTime.longValue + 3000L > System.currentTimeMillis()) {
                        lastBackPressTime.longValue = 0L

                        (context as MainActivity).finish()
                    } else {
                        Toast
                            .makeText(context,
                                context.getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT)
                            .show()
                    }
                    lastBackPressTime.longValue = System.currentTimeMillis()
                }

                WorkSheetScreenContent(
                    workSheet,
                    submission,
                    modifier = Modifier
                        .padding(paddingValues),
                    onSave = { answers, callback ->
                        isLoadingOnSave.value = true
                        scope.launch {
                            saveAnswers(
                                submissionId = id,
                                workSheet.value.data!!,
                                submission.value.data!!,
                                answers,
                                repo
                            )
                            isLoadingOnSave.value = false
                            callback.invoke()
                        }
                    },
                    onSubmit = { answers ->
                        Log.i("Prodigi.Worksheet", "submit.answers: $answers")
                        scope.launch {
                            try {
                                submitAnswer(
                                    id,
                                    workSheetFlow.value.data!!,
                                    submissionFlow.value.data!!,
                                    answers,
                                    repo
                                )
                                onEvaluateSuccess.invoke()
                            } catch (e: Exception) {
                                Log.e("Prodigi.Worksheet", "submit.error: $e")
                            }
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
        Log.i("Prodigi.WorkSheet", "[load.workSheet.$workSheetId] ${conf.data}")
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
        Log.i("Prodigi.WorkSheet", "[load.submission.$submissionId] ${submission.data}")
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
    repo.upsertSubmission(
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
        Log.i("Prodigi.WorkSheet", "[evaluate.answer.$submissionId] $res")
    }
}

@Composable
fun WorkSheetScreenContent(
    workSheetState: State<LoadDataStatus<WorkSheet>>,
    submission: State<LoadDataStatus<Submission>>,
    modifier: Modifier = Modifier,
    onSave: (answers: List<Int>, callback: () -> Unit) -> Unit,
    onSubmit: (answers: List<Int>) -> Unit = {}
) {
    ConstraintLayout(
        modifier
            .padding(horizontal = 20.dp)
            .fillMaxSize(),
    ) {
        val (header, optionsListSection, submitButton) = createRefs()

        if (workSheetState.value is LoadDataStatus.Success && submission.value is LoadDataStatus.Success) {
            val workSheet by remember { derivedStateOf { workSheetState.value.data } }
            val count by remember { derivedStateOf { workSheet?.counts } }
            val options by remember { derivedStateOf { workSheet?.options } }
            val answers = remember {
                mutableStateOf(submission.value.data?.answers ?: List(0) { -1 })
            }
            val isFinished by remember {
                derivedStateOf {
                    answers.value.filter { it != -1 }.size == count
                }
            }
            val isConfirmationOpen = remember { mutableStateOf(false) }

            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(key1 = lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_PAUSE) {
                        Log.i(
                            "Prodigi.Worksheet",
                            "[upsertSubmission.ON_PAUSE] isFinished: $isFinished, $answers"
                        )
                        scope.launch {
                            onSave.invoke(answers.value) {}
                        }
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }
            val onSaveToast = Toast.makeText(
                context,
                R.string.submission_saved,
                Toast.LENGTH_SHORT
            )

            val saveButtonColor = if (isFinished) {
                ButtonDefaults.buttonColors()
            } else {
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val saveButtonLabel = if (isFinished) {
                stringResource(R.string.worksheet_button_finish)
            } else {
                stringResource(R.string.worksheet_button_save)
            }

            if (answers.value.size != count) {
                answers.value = List(count!!) { -1 }
            }

            SubmitConfimationDialog(
                isConfirmationOpen,
                onDismiss = { isConfirmationOpen.value = false },
                onConfirm = {
                    onSubmit.invoke(answers.value)
                    isConfirmationOpen.value = false
                }
            )

            Column(Modifier
                .constrainAs(header) {
                    top.linkTo(parent.top)
                }
                .zIndex(2f)
                .padding(bottom = 20.dp)
                .fillMaxWidth()
            ) {
                Spacer(Modifier.height(30.dp))
                workSheet?.bookTitle?.let { bookTitle ->
                    Text(bookTitle, fontSize = 16.sp)
                }
                workSheet?.contentTitle?.let { contentTitle ->
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

            Button(
                onClick = {
                    if (isFinished) {
                        isConfirmationOpen.value = true
                    } else {
                        onSave.invoke(answers.value) {
                            onSaveToast.show()
                            Log.i(
                                "Prodigi.Worksheet",
                                "[onSave.callback] answers saved: ${answers.value}"
                            )
                        }
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
                Text(saveButtonLabel)
            }
        }

        // Loading State
        if (workSheetState.value is LoadDataStatus.Loading || submission.value is LoadDataStatus.Loading) {
            LoadingState()
        }

        // Error worksheet state
        if (workSheetState.value is LoadDataStatus.Error) {
            val error = workSheetState.value as LoadDataStatus.Error
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
    val workSheet = MutableStateFlow(LoadDataStatus.Success(
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
    )).collectAsState()
    val submission = MutableStateFlow(LoadDataStatus.Success(
        Submission(
            profile = Profile("name", "13", "claasName", "SchoolName"),
            answers = List(10) { -1 }
        )
    )).collectAsState()
    WorkSheetScreenContent(
        workSheet,
        submission,
        onSave = { _, _ -> /* DO NOTHING */ }
    )
}