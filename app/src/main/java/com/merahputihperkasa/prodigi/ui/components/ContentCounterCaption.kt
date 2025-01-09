package com.merahputihperkasa.prodigi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.merahputihperkasa.prodigi.R

@Composable
fun ContentsCounterCaption(
    topAppBarContainerColor: Color,
    dataCount: Int,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(topAppBarContainerColor)
            .padding(horizontal = 20.dp)
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.content_list_counter_caption, dataCount),
            style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}