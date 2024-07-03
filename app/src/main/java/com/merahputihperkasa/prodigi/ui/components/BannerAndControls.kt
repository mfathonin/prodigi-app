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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

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
    if (isPortrait) {
        Box(
            modifier = modifier.fillMaxSize()
        ) {
            val images = listOf(
                "https://media.npr.org/assets/img/2021/08/11/gettyimages-1279899488_wide-f3860ceb0ef19643c335cb34df3fa1de166e2761-s1100-c50.jpg",
                "https://cdn.pixabay.com/photo/2017/02/20/18/03/cat-2083492__480.jpg",
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRrfPnodZbEjtJgE-67C-0W9pPXK8G9Ai6_Rw&usqp=CAU",
                "https://i.ytimg.com/vi/E9iP8jdtYZ0/maxresdefault.jpg",
                "https://cdn.shopify.com/s/files/1/0535/2738/0144/articles/shutterstock_149121098_360x.jpg"
            )

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
                ImageSlider(
                    images,
                    imageHeight = imageHeight,
                    imageWidth = imageWidth
                )
                ButtonControlsPortrait(
                    onHistoryClick = onHistoryClick,
                    onFlashClick = onFlashClick
                )
            }
        }
    }
}