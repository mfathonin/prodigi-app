package com.merahputihperkasa.prodigi.ui.components

import android.content.Context
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.merahputihperkasa.prodigi.R
import com.merahputihperkasa.prodigi.data.ProdigiRepositoryImpl
import com.merahputihperkasa.prodigi.models.Content
import com.merahputihperkasa.prodigi.network.ApiResult
import com.merahputihperkasa.prodigi.network.RetrofitInstance
import com.merahputihperkasa.prodigi.ui.theme.Secondary800
import com.merahputihperkasa.prodigi.ui.theme.Typography
import com.merahputihperkasa.prodigi.utils.copyToClipboard
import com.merahputihperkasa.prodigi.utils.isValidURi
import com.merahputihperkasa.prodigi.utils.openUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultBottomSheet(
    result: String, onDismissRequest: () -> Unit, sheetState: SheetState,
) {
    val isValidUrl = isValidURi(result)
    // TODO: This need to be more configurable from remote or centralized config
    val isInternalSources = isValidUrl && result.contains("mpp-hub.netlify.app/links")
    val contentId = result.substringAfter("links/")

    ModalBottomSheet(
        onDismissRequest = { onDismissRequest() },
        sheetState = sheetState
    ) {
        val scope = rememberCoroutineScope()
        val context = LocalContext.current

        val prodigiRepository = ProdigiRepositoryImpl(RetrofitInstance.api)
        val content = remember {
            MutableStateFlow<ApiResult<List<Content>>>(ApiResult.Loading())
        }
        val contentResult = content.collectAsState()

        LaunchedEffect(key1 = isInternalSources) {
            if (!isInternalSources) return@LaunchedEffect

            scope.launch {
                fetchContents(prodigiRepository, context, contentId, content)
            }
        }

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
                    style = Typography.titleLarge,
                )
                if (!isInternalSources) {
                    Column(Modifier.fillMaxWidth()) {
                        Text(text = result, modifier = Modifier.fillMaxWidth())
                        Button(
                            onClick = {
                                if (isValidUrl) openUrl(context, result)
                                else copyToClipboard(context, result)

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
                                Text(stringResource(R.string.url_link_button), color = Color.White)
                            } else {
                                Text(
                                    stringResource(R.string.content_link_button),
                                    color = Color.White
                                )
                            }
                        }
                    }
                } else {
                    when (contentResult.value) {
                        is ApiResult.Loading -> {
                            // TODO: Add loading animation here
                            Text("Loading data", modifier = Modifier.padding(top = 24.dp))
                        }

                        is ApiResult.Error -> {
                            Column {
                                Text(
                                    stringResource(R.string.fetch_content_error),
                                    modifier = Modifier.padding(top = 24.dp)
                                )
                                Button(
                                    onClick = {
                                        scope.launch {
                                            fetchContents(
                                                prodigiRepository,
                                                context,
                                                contentId,
                                                content
                                            )
                                        }
                                    }
                                ) {
                                    Text(stringResource(R.string.retry_button))
                                }
                            }
                        }

                        is ApiResult.Success -> {
                            Text(
                                stringResource(id = R.string.subtitle_content_list),
                                style = Typography.labelSmall,
                                color = Secondary800,
                                modifier = Modifier.padding(bottom = 24.dp, top = 4.dp)
                            )
                            val data = (contentResult.value as ApiResult.Success).data
                            data?.forEach { content ->
                                ContentCardView(content) {
                                    scope.launch {
                                        sheetState.hide()
                                        onDismissRequest.invoke()

                                        // TODO: Record to history
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun fetchContents(
    prodigiRepository: ProdigiRepositoryImpl,
    context: Context,
    contentId: String,
    content: MutableStateFlow<ApiResult<List<Content>>>,
) {
    content.update { ApiResult.Loading() }
    prodigiRepository.getDigitalContents(context, contentId)
        .collectLatest { result ->
            content.update { result }
        }
}