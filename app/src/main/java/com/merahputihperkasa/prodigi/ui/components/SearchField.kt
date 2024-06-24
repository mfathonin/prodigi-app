package com.merahputihperkasa.prodigi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.merahputihperkasa.prodigi.R
import com.merahputihperkasa.prodigi.ui.theme.Surface400

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
) {
    val visualTransformation: VisualTransformation = VisualTransformation.None
    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    val colors = OutlinedTextFieldDefaults.colors().copy(
        focusedTextColor = Color.DarkGray,
        unfocusedTextColor = Color.DarkGray,
        focusedContainerColor = Surface400,
        unfocusedContainerColor = Surface400,
        focusedPlaceholderColor = Color.DarkGray.copy(0.5f),
        unfocusedPlaceholderColor = Color.DarkGray.copy(0.5f),
        cursorColor = MaterialTheme.colorScheme.primary,
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp),
    ) {
        BasicTextField(
            value = value,
            modifier = Modifier
                .fillMaxWidth(),
            onValueChange = onValueChange,
            singleLine = true,
            maxLines = 1,
            textStyle = MaterialTheme.typography.bodyLarge,
            decorationBox = @Composable { innerTextField ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value = value,
                    visualTransformation = visualTransformation,
                    innerTextField = innerTextField,
                    colors = colors,
                    placeholder = {
                        Text(
                            stringResource(R.string.search_hint),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    singleLine = true,
                    enabled = true,
                    interactionSource = interactionSource,
                    contentPadding = PaddingValues(
                        top = 6.dp, bottom = 6.dp,
                        start = 40.dp, end = 16.dp
                    ),
                    container = {
                        Row(
                            modifier = Modifier
                                .border(
                                    color = Color.DarkGray.copy(0.4f),
                                    width = 1.dp,
                                    shape = CircleShape
                                )
                                .clip(CircleShape)
                                .background(Surface400)
                                .padding(vertical = 6.dp)
                                .padding(horizontal = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = Color.LightGray
                            )
                        }
                    }
                )
            }
        )
    }
}