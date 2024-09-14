package com.merahputihperkasa.prodigi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.merahputihperkasa.prodigi.ProdigiApp
import com.merahputihperkasa.prodigi.models.BannerItem
import com.merahputihperkasa.prodigi.repository.LoadDataStatus
import com.merahputihperkasa.prodigi.repository.ProdigiRepositoryImpl
import com.merahputihperkasa.prodigi.utils.openUrl
import kotlinx.coroutines.flow.collectLatest

@Composable
fun BannerAndControls(
    modifier: Modifier = Modifier,
    onHistoryClick: () -> Unit,
    onFlashClick: () -> Boolean,
) {
    val configuration = LocalConfiguration.current
    val screenW = configuration.screenWidthDp
    val screenH = configuration.screenHeightDp
    val resolution = 16 / 9f
    var imageWidth = (screenW - 24 - 24).dp
    var imageHeight = imageWidth / resolution

    var maxImageHeight = 200f
    maxImageHeight = if (0.35 * screenH > maxImageHeight) {
        maxImageHeight
    } else {
        0.35f * screenH
    }

    if (imageHeight.value > maxImageHeight) {
        imageHeight = maxImageHeight.dp
        imageWidth = (maxImageHeight * resolution).dp
    }
    val isPortrait = screenW < screenH

    val context = LocalContext.current
    val prodigiRepository = ProdigiRepositoryImpl(ProdigiApp.appModule)
    var bannerItems by remember { mutableStateOf<List<BannerItem>>(emptyList()) }

    LaunchedEffect(key1 = Unit) {
        prodigiRepository.getBannerItems().collectLatest { result ->
            if (result is LoadDataStatus.Success) {
                bannerItems = result.data ?: emptyList()
            }
        }
    }

    if (isPortrait) {
        Box(
            modifier = modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .width(imageWidth + 24.dp + 24.dp)
                    .padding(vertical = 16.dp, horizontal = 12.dp)
                    .padding(bottom = 8.dp)
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (bannerItems.isNotEmpty()) {
                    ImageSlider(
                        images = bannerItems.map { it.image },
                        imageHeight = imageHeight,
                        imageWidth = imageWidth,
                        onImageClick = { index ->
                            bannerItems.getOrNull(index)?.let { item ->
                                openUrl(context, item.url)
                            }
                        }
                    )
                }
                ButtonControlsPortrait(
                    onHistoryClick = onHistoryClick,
                    onFlashClick = onFlashClick
                )
            }
        }
    }
}