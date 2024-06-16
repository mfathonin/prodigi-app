package com.merahputihperkasa.prodigi

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merahputihperkasa.prodigi.utils.copyToClipboard
import com.merahputihperkasa.prodigi.utils.isValidURi
import com.merahputihperkasa.prodigi.utils.openUrl
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultBottomSheet(
    result: String, onDismissRequest: () -> Unit, sheetState: SheetState,
) {
    val isValidUrl = isValidURi(result)
    // TODO: This need to be more configurable from remote or centralized config
    val isInternalSources = isValidUrl && result.contains("mpp-hub.netlify.app/links")

    ModalBottomSheet(
        onDismissRequest = { onDismissRequest() },
        sheetState = sheetState
    ) {
        val scope = rememberCoroutineScope()
        var titleId = R.string.internal_content_title
        if (!isInternalSources) {
            titleId = R.string.external_content_title
        }

        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
            ) {
                Text(
                    text = stringResource(titleId),
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                )
                if (!isInternalSources) {
                    Column(Modifier.fillMaxWidth()) {
                        val context = LocalContext.current
                        Text(text = result, modifier = Modifier.fillMaxWidth())
                        Button(
                            onClick = {
                                if (isValidUrl)
                                    openUrl(context, result)
                                else
                                    copyToClipboard(context, result)

                                scope.launch {
                                    sheetState.hide()
                                    onDismissRequest.invoke()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = 16.dp),
                        ) {
                            if (isValidUrl) {
                                Text(
                                    text = stringResource(R.string.url_link_button),
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.content_link_button),
                                    color = Color.White
                                )
                            }
                        }
                    }
                } else {
                    // TODO: Render list items
                    Text(text = "Redirecting to: $result", modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}