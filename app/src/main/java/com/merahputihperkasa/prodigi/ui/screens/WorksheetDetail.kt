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
fun WorkSheetDetailScreen(id: String, onNavigateStart: (id: Int, worksheetId: String) -> Unit) {
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
        val workSheetConf = workSheetConfFlow.collectAsState()

        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(key1 = lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    scope.launch {
                        loadConfiguration(id, repo, workSheetConfFlow)
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
                Column(
                    Modifier
                        .padding(paddingValues)
                        .padding(horizontal = 20.dp)
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    Arrangement.Center
                ) {
                    Text(
                        stringResource(R.string.worksheet_title),
                        modifier = Modifier.fillMaxWidth().padding(top = 50.dp, bottom = 20.dp),
                        textAlign = TextAlign.Center, fontSize = 24.sp, fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(30.dp))
                    when (workSheetConf.value) {
                        is LoadDataStatus.Loading -> {
                            LoadingState()
                        }
                        is LoadDataStatus.Error -> {
                            val error = workSheetConf.value as LoadDataStatus.Error
                            ErrorState(
                                error = error,
                                errorDescriptor = stringResource(R.string.error_load_worksheet)
                            )
                        }
                        else -> {
                            val conf = (workSheetConf.value as LoadDataStatus.Success).data
                            if (conf != null) {
                                conf.bookTitle?.let { bookTitle ->
                                    Text(bookTitle, fontSize = 18.sp)
                                }
                                conf.contentTitle?.let { contentTitle ->
                                    Text(
                                        contentTitle,
                                        modifier = Modifier
                                            .padding(vertical = 4.dp)
                                            .padding(bottom = 8.dp),
                                        fontSize = 28.sp, fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(stringResource(R.string.worksheet_question_numbers, conf.counts))

                                Spacer(Modifier.height(50.dp))

                                // TODO:
                                //  Create form for Profile and start worksheet creation
                                //  worksheet creation based on counts and n_options in conf
                                ProfileForm(conf.uuid, conf.counts) { id ->
                                    onNavigateStart.invoke(id, conf.uuid)
                                }
                            } else {
                                EmptyState(
                                    dataDescription = stringResource(R.string.error_worksheet_descriptor)
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}

private suspend fun loadConfiguration(
    id: String,
    repo: ProdigiRepositoryImpl,
    stateFlow: MutableStateFlow<LoadDataStatus<WorkSheet>>
) {
    repo.getWorkSheetConfig(id, false).collectLatest { conf ->
        stateFlow.update { conf }
    }
}