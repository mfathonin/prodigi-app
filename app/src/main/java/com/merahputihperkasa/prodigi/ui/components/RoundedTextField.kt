package com.merahputihperkasa.prodigi.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.merahputihperkasa.prodigi.ui.theme.ProdigiBookReaderTheme

data class TextFieldState(
    val message: String,
    val error: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoundedTextField(
    modifier: Modifier = Modifier.fillMaxWidth(),
    label: String? = null,
    value: String,
    state: TextFieldState? = null,
    required: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    onValueChange: (String) -> Unit,
) {
    val interactionSource by remember { mutableStateOf(MutableInteractionSource()) }
    Column {
        if (label != null) {
            Row {
                Text(
                    label,
                    modifier = Modifier.padding(bottom = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                )
                if (required) {
                    Text(
                        "*",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.W400
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            interactionSource = interactionSource,
        ) { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value,
                innerTextField = innerTextField,
                enabled = true,
                isError = state?.error == true,
                placeholder = {
                    if (label != null) {
                        Text(
                            label,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .3f),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                },
                supportingText = { if (state !=null ) {
                    Text(
                        state.message,
                        color = state.error.let { error ->
                            if (error) {MaterialTheme.colorScheme.error } else { MaterialTheme.colorScheme.surfaceTint.copy(alpha = .7f) }
                        }
                    )
                }},
                singleLine = true,
                contentPadding = PaddingValues(vertical = 10.dp, horizontal = 16.dp),
                visualTransformation = VisualTransformation.None,
                interactionSource = interactionSource,
            ) {
                OutlinedTextFieldDefaults.Container(
                    shape = MaterialTheme.shapes.medium,
                    isError = state?.error == true,
                    enabled = true,
                    interactionSource = interactionSource,
                    focusedBorderThickness = 1.7.dp,
                    unfocusedBorderThickness = 1.dp,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceTint.copy(alpha = .5f),
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RoundedTextFieldPreview(modifier: Modifier = Modifier) {
    ProdigiBookReaderTheme(darkTheme = false) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(20.dp),
            Arrangement.spacedBy(10.dp)
        ) {
            RoundedTextField(
                label = "Name",
                value = "",
                state = TextFieldState("This field is required"),
                required = true,
                onValueChange = {}
            )
            RoundedTextField(
                label = "Name",
                state = TextFieldState("This field is error", error = true),
                value = "",
                onValueChange = {}
            )
        }
    }
}