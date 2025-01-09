package com.merahputihperkasa.prodigi.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkSheetScreen(id: Int, workSheetId: String, onEvaluateSuccess: (id: Int?, submission: Submission?) -> Unit) {
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

                CompositionLocalProvider(
                    LocalOverscrollConfiguration provides null
                ) {
                    WorkSheetScreenContent(
                        workSheet,
                        submission,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
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
                            isLoadingOnSave.value = true
                            scope.launch {
                                try {
                                    val updatedSubmission = submitAnswer(
                                        id,
                                        workSheetFlow.value.data!!,
                                        submissionFlow.value.data!!,
                                        answers,
                                        repo
                                    )

                                    onEvaluateSuccess.invoke(id, updatedSubmission)
                                } catch (e: Exception) {
                                    Log.e("Prodigi.Worksheet", "[submit.error.$id]: $e")
                                } finally {
                                    isLoadingOnSave.value = false
                                }
                            }
                        }
                    )
                }
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
): Submission? {
    // cap the answer to worksheet.counts
    var savedSubmission: Submission? = null

    val cleanAnswers = answers.take(workSheet.counts)
    repo.submitEvaluateAnswer(
        submissionId,
        workSheet.uuid,
        submission.copy(
            answers = cleanAnswers
        )
    ).collectLatest { res ->
        Log.i("Prodigi.WorkSheet", "[evaluate.answer.$submissionId] ${res.data}")
        res.data.let { subs ->
            if (subs != null) {
                savedSubmission = subs.copy(
                    answers = cleanAnswers
                )
            }
        }
    }

    return savedSubmission
}

@Composable
fun WorkSheetScreenContent(
    workSheetState: State<LoadDataStatus<WorkSheet>>,
    submission: State<LoadDataStatus<Submission>>,
    modifier: Modifier = Modifier,
    onSave: (answers: List<Int>, callback: () -> Unit) -> Unit,
    onSubmit: (answers: List<Int>) -> Unit = {}
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

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

            if (answers.value.size != count) {
                answers.value = List(count!!) { -1 }
            }

            val isAnswersNotSave = remember { mutableStateOf(false) }

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
                Row(Modifier.fillMaxWidth(), Arrangement.Start) {
                    Column(
                        Modifier
                            .size(44.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.shapes.large
                            ),
                        Arrangement.Center,
                        Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            painter = painterResource(R.mipmap.ic_logo),
                            contentDescription = "Question Number",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(Modifier.width(15.dp))
                    Column(verticalArrangement = Arrangement.SpaceBetween) {
                        workSheet?.bookTitle?.let { bookTitle ->
                            Text(
                                bookTitle,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.W400
                            )
                        }
                        workSheet?.contentTitle?.let { contentTitle ->
                            Text(
                                contentTitle,
                                modifier = Modifier.padding(vertical = 4.dp),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Thin
                            )
                        }
                    }
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
                        ) {
                            isAnswersNotSave.value = true
                        }

                        // Border for non last element
                        if (index != optionsItems.size - 1) {
                            Spacer(Modifier.height(15.dp))
                            HorizontalDivider(
                                Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color.Transparent,
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = .1f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                                    .align(Alignment.CenterHorizontally),
                                color = Color.Transparent,
                            )
                        } else {
                            Box(Modifier.padding(top = 10.dp, bottom = 30.dp))
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .constrainAs(submitButton) {
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end)
                        start.linkTo(parent.start)
                    }
                    .padding(bottom = 20.dp, top = 10.dp)
                    .requiredWidth(screenWidth - 40.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    enabled = isFinished,
                    onClick = {
                        isConfirmationOpen.value = true
                    },
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .zIndex(2f)
                        .weight(1f)
                ) {
                    Text(
                        stringResource(
                            R.string.worksheet_button_finish,
                            answers.value.filter { it >= 0 }.size,
                            count!!
                        )
                    )
                }
                Spacer(Modifier.width(10.dp))
                IconButton(
                    enabled = isAnswersNotSave.value,
                    onClick = {
                        onSave.invoke(answers.value) {
                            onSaveToast.show()
                            Log.i(
                                "Prodigi.Worksheet",
                                "[onSave.callback] answers saved: ${answers.value}"
                            )
                        }
                        isAnswersNotSave.value = false
                    },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceTint,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Box(Modifier.size(32.dp).padding(7.dp)) {
                        Icon(
                            painter = painterResource(R.drawable.save),
                            "Share Button",
                            Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
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
            counts = 4,
            options = listOf(4,4,3,5),
            points = List(4) { 5 }
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