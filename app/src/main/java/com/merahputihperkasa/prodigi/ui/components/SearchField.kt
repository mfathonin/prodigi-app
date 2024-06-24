package com.merahputihperkasa.prodigi.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.merahputihperkasa.prodigi.R
import com.merahputihperkasa.prodigi.ui.theme.Typography

@Composable
fun SearchField(
    modifier: Modifier = Modifier,
    searchValue: String,
    onSearchValueChange: (String) -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp),
    ) {
        OutlinedTextField(
            value = searchValue,
            onValueChange = { value ->
                onSearchValueChange.invoke(value)
            },
            placeholder = {
                Text(stringResource(R.string.search_hint))
            },
            textStyle = Typography.bodyMedium,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
        )
    }
}