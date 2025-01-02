package com.merahputihperkasa.prodigi.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.merahputihperkasa.prodigi.ProdigiApp
import com.merahputihperkasa.prodigi.R
import com.merahputihperkasa.prodigi.repository.ProdigiRepositoryImpl
import kotlinx.coroutines.launch

@Composable
fun ProfileForm(workSheetId: String, onSubmitted: () -> Unit) {
    val context = LocalContext.current
    val repo = ProdigiRepositoryImpl(ProdigiApp.appModule, context)

    var name by rememberSaveable { mutableStateOf("") }
    var idNumber by rememberSaveable { mutableStateOf("") }
    var className by rememberSaveable { mutableStateOf("") }
    var schoolName by rememberSaveable { mutableStateOf("") }
    val isFormValid by remember {
        derivedStateOf {
            name.isNotBlank() && idNumber.isNotBlank() && className.isNotBlank() && schoolName.isNotBlank()
        }
    }
    val scope = rememberCoroutineScope()

    OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text(stringResource(R.string.profile_field_name)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = idNumber,
        onValueChange = { idNumber = it },
        label = { Text(stringResource(R.string.profile_field_id_number)) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        singleLine = true
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = className,
        onValueChange = { className = it },
        label = { Text(stringResource(R.string.profile_field_class_name)) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        singleLine = true
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = schoolName,
        onValueChange = { schoolName = it },
        label = { Text(stringResource(R.string.profile_field_school_name)) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        singleLine = true
    )
    Spacer(modifier = Modifier.height(32.dp))
    Button(
        onClick = {
            if (isFormValid) {
                scope.launch {
                    // Save data to Room database
                    repo.saveProfile(workSheetId, name, idNumber, className, schoolName)

                    // Navigate to the next screen
                    onSubmitted.invoke()
                }
            }
        },
        enabled = isFormValid,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.worksheet_start_button_label))
    }
    Spacer(modifier = Modifier.height(40.dp))
}