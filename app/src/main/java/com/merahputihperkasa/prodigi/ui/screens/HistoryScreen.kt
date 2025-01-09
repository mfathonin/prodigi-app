package com.merahputihperkasa.prodigi.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.merahputihperkasa.prodigi.ProdigiApp
import com.merahputihperkasa.prodigi.R
import com.merahputihperkasa.prodigi.models.ContentEntity
import com.merahputihperkasa.prodigi.models.toContent
import com.merahputihperkasa.prodigi.repository.LoadDataStatus
import com.merahputihperkasa.prodigi.repository.ProdigiRepositoryImpl
import com.merahputihperkasa.prodigi.ui.components.CenterTopAppBarTitle
import com.merahputihperkasa.prodigi.ui.components.ContentCardView
import com.merahputihperkasa.prodigi.ui.components.ContentsCounterCaption
import com.merahputihperkasa.prodigi.ui.components.SearchField
import com.merahputihperkasa.prodigi.ui.theme.ProdigiBookReaderTheme
import com.merahputihperkasa.prodigi.ui.theme.Surface400
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController, modifier: Modifier = Modifier) {
    ProdigiBookReaderTheme {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            rememberTopAppBarState()
        )
        var searchValue by remember {
            mutableStateOf("")
        }

        val isCollapsed by remember {
            derivedStateOf {
                scrollBehavior.state.collapsedFraction > 0.5f
            }
        }
        val topAppBarContainerColor = if (isCollapsed) {
            MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
        } else {
            MaterialTheme.colorScheme.surface
        }

        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val prodigiRepository = ProdigiRepositoryImpl(ProdigiApp.appModule, context)

        val contentListFlow = remember {
            MutableStateFlow<LoadDataStatus<List<ContentEntity>>>(LoadDataStatus.Loading())
        }
        val contentList = contentListFlow.collectAsState()

        val dataCount: Int = if (contentList.value is LoadDataStatus.Success) {
            contentList.value.data.let {
                if (it.isNullOrEmpty()) 0 else it.size
            }
        } else 0

        LaunchedEffect(key1 = searchValue) {
            if (searchValue.isNotEmpty()) {
                scope.launch {
                    loadContents(contentListFlow, prodigiRepository, searchValue)
                    Log.i("Prodigi.History", "[loadContents.query.$searchValue] $contentListFlow")
                }
            }
        }

        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(key1 = lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    scope.launch {
                        loadContents(contentListFlow, prodigiRepository, searchValue)
                        Log.i("Prodigi.History", "[loadContents.ON_RESUME] $contentListFlow")
                    }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        Scaffold(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
            LargeTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                scrollBehavior = scrollBehavior,
                title = { CenterTopAppBarTitle(scrollBehavior = scrollBehavior) },
                actions = { Box(Modifier.width(36.dp)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors().copy(
                    containerColor = topAppBarContainerColor
                )
            )
        }, content = { paddingValues ->
            Column(
                modifier = modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                SearchField(
                    value = searchValue,
                    modifier = Modifier.background(topAppBarContainerColor)
                ) { value -> searchValue = value }
                if (contentList.value is LoadDataStatus.Success) {
                    ContentsCounterCaption(topAppBarContainerColor, dataCount)
                }

                val roundSize = (24 * (1 - scrollBehavior.state.collapsedFraction)).dp
                val shape = RoundedCornerShape(topStart = roundSize, topEnd = roundSize)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(topAppBarContainerColor)
                        .clip(shape)
                        .background(Surface400)
                        .verticalScroll(rememberScrollState())
                        .padding(top = 8.dp)
                        .padding(16.dp),

                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (contentList.value) {
                        is LoadDataStatus.Loading -> {
                            LoadingState()
                        }

                        is LoadDataStatus.Error -> {
                            val error = contentList.value as LoadDataStatus.Error
                            ErrorState(
                                error = error,
                                errorDescriptor = stringResource(R.string.content_error_load)
                            )
                        }

                        else -> {
                            val contents = (contentList.value as LoadDataStatus.Success).data
                            if (contents.isNullOrEmpty()) {
                                EmptyState(dataDescription = stringResource(R.string.content_empty_descriptor))
                            } else {
                                contents.forEach { contentEntity ->
                                    val content = contentEntity.toContent()
                                    ContentCardView(content, adaptiveColor = false)
                                }
                            }
                        }
                    }
                }
            }

        })
    }
}

private suspend fun loadContents(
    contentList: MutableStateFlow<LoadDataStatus<List<ContentEntity>>>,
    prodigiRepository: ProdigiRepositoryImpl,
    searchValue: String,
) {
    prodigiRepository.getFilteredContents(searchValue, false).collectLatest { value ->
        contentList.update { value }
    }
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(top = 50.dp)
    ) {
        CircularProgressIndicator()
        Text(
            stringResource(R.string.general_load_data), modifier = Modifier.padding(8.dp), color = Color.Black
        )
    }
}

@Composable
fun <T> ErrorState(
    modifier: Modifier = Modifier,
    error: LoadDataStatus.Error<T>,
    errorDescriptor: String = "",
) {
    Column(
        modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.mipmap.no_documents),
            contentDescription = "Error on load",
            modifier = Modifier
                .height(180.dp)
                .padding(vertical = 24.dp)
        )
        Text(
            stringResource(R.string.general_error_msg, errorDescriptor), color = Color.Black
        )
        error.message?.let { msg ->
            Text(
                "Code: $msg", color = Color.Black.copy(alpha = 0.5f),
                style = MaterialTheme.typography.labelSmall
                    .copy(fontStyle = FontStyle.Italic)
            )
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier, dataDescription: String) {
    Column(
        modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.mipmap.no_search_result),
            contentDescription = "Error on load",
            modifier = Modifier
                .height(180.dp)
                .padding(vertical = 24.dp)
        )
        Text(
            stringResource(R.string.general_no_data, dataDescription), color = Color.Black
        )
    }
}