package com.merahputihperkasa.prodigi.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.res.stringResource
import com.merahputihperkasa.prodigi.R

@Composable
fun SubmitConfimationDialog(isOpen: State<Boolean>, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    if (isOpen.value) {
        AlertDialog(
            onDismissRequest = {
                onDismiss.invoke()
            },
            dismissButton = {
                Button(
                    onClick = { onDismiss.invoke() },
                    colors = ButtonDefaults.outlinedButtonColors()
                ) {
                    Text(stringResource(R.string.general_cancle_button_label))
                }
            },
            confirmButton = {
                Button( onClick = { onConfirm.invoke() } ) {
                    Text(stringResource(R.string.worksheet_submit_button_label))
                }
            },
            title = { Text(stringResource(R.string.worksheet_confirmation_title)) },
            text = { Text(stringResource(R.string.worksheet_confirmation_desc)) }
        )
    }
}