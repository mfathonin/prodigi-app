package com.merahputihperkasa.prodigi.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merahputihperkasa.prodigi.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CenterTopAppBarTitle(modifier: Modifier = Modifier, scrollBehavior: TopAppBarScrollBehavior) {
    val expanded = 32
    val collapsed = 24
    val fontSize =
        (collapsed + (expanded - collapsed) * (1 - scrollBehavior.state.collapsedFraction)).sp

    val style = TextStyle(
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = fontSize,
    )

    Text(
        stringResource(R.string.history_title),
        modifier
            .fillMaxWidth()
            .padding(end = 16.dp),
        textAlign = TextAlign.Center,
        style = style
    )
}

