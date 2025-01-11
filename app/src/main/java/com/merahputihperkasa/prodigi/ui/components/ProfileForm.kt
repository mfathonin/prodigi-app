package com.merahputihperkasa.prodigi.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.merahputihperkasa.prodigi.ProdigiApp
import com.merahputihperkasa.prodigi.R
import com.merahputihperkasa.prodigi.models.Profile
import com.merahputihperkasa.prodigi.models.SubmissionEntity
import com.merahputihperkasa.prodigi.models.WorkSheet
import com.merahputihperkasa.prodigi.repository.LoadDataStatus
import com.merahputihperkasa.prodigi.repository.ProdigiRepositoryImpl
import com.merahputihperkasa.prodigi.ui.screens.loadSubmissionHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import sv.lib.squircleshape.CornerSmoothing
import sv.lib.squircleshape.SquircleShape

@Composable
fun ProfileForm(
    workSheet: WorkSheet,
    submission: SubmissionEntity? = null,
    onNavigateToHistories: (workSheet: WorkSheet) -> Unit = {},
    onSubmitted: (id: Int) -> Unit = {},
) {
    val context = LocalContext.current
    val repo = ProdigiRepositoryImpl(ProdigiApp.appModule, context)
    val profile = submission?.toSubmission()?.profile

    var name by rememberSaveable { mutableStateOf(profile?.name ?: "") }
    var idNumber by rememberSaveable { mutableStateOf(profile?.idNumber ?: "") }
    var className by rememberSaveable { mutableStateOf(profile?.className ?: "") }
    var schoolName by rememberSaveable { mutableStateOf(profile?.schoolName ?: "") }
    val isFormValid by remember {
        derivedStateOf {
            name.isNotBlank() && idNumber.isNotBlank() && className.isNotBlank() && schoolName.isNotBlank()
        }
    }
    val scope = rememberCoroutineScope()

    val submissionHistoriesFlow = remember {
        MutableStateFlow<LoadDataStatus<List<SubmissionEntity>>>(LoadDataStatus.Loading())
    }
    val submissionCount: Int = submissionHistoriesFlow.collectAsState().value.data?.size ?: 0

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scope.launch {
                    loadSubmissionHistory(workSheet.uuid, submissionHistoriesFlow, repo)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val keyboardOptions = KeyboardOptions(
        imeAction = ImeAction.Next,
        capitalization = KeyboardCapitalization.Words
    )

    var containerWidth = LocalConfiguration.current.screenWidthDp.dp - 40.dp
    if (containerWidth > 500.dp) {
        containerWidth = 500.dp
    }

    val borderShape = SquircleShape(32.dp, CornerSmoothing.High)
    Column(
        Modifier
            .requiredWidth(containerWidth)
            .clip(borderShape)
            .border(
                BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.surfaceTint.copy(alpha = .7f)
                ),
                borderShape
            )
            .padding(15.dp),
        Arrangement.spacedBy(4.dp)
    ) {
        Column {
            Text(
                stringResource(R.string.profile_form_title),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.profile_form_desc),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .4f),
                fontWeight = FontWeight.Light,
            )
        }
        Spacer(Modifier.height(12.dp))
        RoundedTextField(
            label = stringResource(R.string.profile_field_name),
            value = name,
            onValueChange = { name = it },
            required = true,
            keyboardOptions = keyboardOptions
        )
        val rowWidth = containerWidth - 30.dp
        Row(Modifier.requiredWidth(rowWidth)) {
            val fieldWidth = (rowWidth - 10.dp) / 2
            RoundedTextField(
                value = className,
                onValueChange = { className = it },
                label = stringResource(R.string.profile_field_class_name),
                required = true,
                modifier = Modifier.requiredWidth(fieldWidth),
                keyboardOptions = keyboardOptions,
            )
            Spacer(Modifier.width(10.dp))
            RoundedTextField(
                value = idNumber,
                onValueChange = { idNumber = it },
                label = stringResource(R.string.profile_field_id_number),
                keyboardOptions = keyboardOptions,
                required = true,
                modifier = Modifier.requiredWidth(fieldWidth),
            )
        }
        RoundedTextField(
            value = schoolName,
            onValueChange = { schoolName = it },
            label = stringResource(R.string.profile_field_school_name),
            required = true,
            keyboardOptions = keyboardOptions.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions {
                if (isFormValid) {
                    scope.launch {
                        handleSubmitProfile(
                            repo, workSheet, Profile(
                                name, idNumber, className, schoolName
                            ), submission, onSubmitted
                        )

                        // Reset form state
                        name = ""
                        idNumber = ""
                        className = ""
                        schoolName = ""
                    }
                }
            }
        )
    }

    Spacer(modifier = Modifier.height(20.dp))
    Row(
        modifier = Modifier.height(36.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            onClick = {
                onNavigateToHistories.invoke(workSheet)
            },
            enabled = submissionCount > 0,
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.outlinedButtonColors(),
            contentPadding = PaddingValues(horizontal = 12.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.file_clock),
                "History Button",
                modifier = Modifier.size(20.dp).padding(end = 5.dp)
            )
            Text(stringResource(R.string.worksheet_history_button_label))
        }
        Button(
            onClick = {
                if (isFormValid) {
                    scope.launch {
                        handleSubmitProfile(
                            repo, workSheet, Profile(
                                name, idNumber, className, schoolName
                            ), submission
                        ) {
                            onSubmitted.invoke(it)
                        }

                        // Reset form state
                        name = ""
                        idNumber = ""
                        className = ""
                        schoolName = ""
                    }
                }
            },
            enabled = isFormValid,
            shape = MaterialTheme.shapes.large,
            contentPadding = PaddingValues(horizontal = 20.dp),
        ) {
            Text(stringResource(R.string.worksheet_start_button_label))
        }
    }
    Spacer(modifier = Modifier.height(15.dp))
}

suspend fun handleSubmitProfile(
    repo: ProdigiRepositoryImpl,
    workSheet: WorkSheet,
    profile: Profile,
    submissionEntity: SubmissionEntity? = null,
    onSubmitted: (id: Int) -> Unit = {}
) {
    val workSheetId = workSheet.uuid
    val count = workSheet.counts
    val submission = submissionEntity?.toSubmission()
    val answers = submission?.answers

    // Save data to Room database
    val id = repo.upsertSubmission(
        id = submissionEntity?.id, workSheetId,
        profile.name, profile.idNumber, profile.className, profile.schoolName,
        answers = answers ?: List(count) { -1 }
    )

    // Navigate to the next screen
    onSubmitted.invoke(submissionEntity?.id ?: id)
}