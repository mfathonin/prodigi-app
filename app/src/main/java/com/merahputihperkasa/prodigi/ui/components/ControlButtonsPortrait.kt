package com.merahputihperkasa.prodigi.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.merahputihperkasa.prodigi.R

@Composable
fun ButtonControlsPortrait(
    modifier: Modifier = Modifier,
    onHistoryClick: () -> Unit,
    onFlashClick: () -> Boolean,
) {
    var isFlashEnabled by remember {
        mutableStateOf(false)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onHistoryClick,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.rounded_lists),
                contentDescription = "History",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        VerticalDivider(
            modifier = Modifier.height(16.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )

        IconButton(
            onClick = {
                isFlashEnabled = onFlashClick()
            },
        ) {
            val flashIcon = if (isFlashEnabled) {
                R.drawable.round_flash_on
            } else R.drawable.round_flash_off
            Icon(
                painterResource(id = flashIcon),
                contentDescription = "Flash button",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}