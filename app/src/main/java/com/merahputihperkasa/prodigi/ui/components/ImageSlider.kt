package com.merahputihperkasa.prodigi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

@Composable
fun ImageSlider(
    images: List<String>,
    imageHeight: Dp,
    imageWidth: Dp,
    onImageClick: (Int) -> Unit
) {
    var currentImageIndex by remember { mutableIntStateOf(0) }
    val scrollState = rememberLazyListState()

    Column {
        Box(
            modifier = Modifier
                .height(imageHeight + 24.dp)
                .width(imageWidth + 24.dp)
                .shadow(elevation = 3.dp, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
                .padding(12.dp)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                state = scrollState,
                userScrollEnabled = false,
            ) {
                itemsIndexed(images) { index, image ->
                    Card(
                        modifier = Modifier
                            .width(imageWidth)
                            .height(imageHeight)
                            .clickable { onImageClick(index) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        AsyncImage(
                            model = image,
                            contentDescription = "Banner image $index",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            userScrollEnabled = false,
        ) {
            items(images.size) { id ->
                DotIndicator(isActive = id == currentImageIndex)
            }
        }
    }

    // Auto-play the slider
    LaunchedEffect(currentImageIndex) {
        while (true) {
            delay(5000L)
            val nextIndex = (currentImageIndex + 1) % images.size
            scrollState.animateScrollToItem(nextIndex)
            currentImageIndex = nextIndex
        }
    }
}


@Composable
fun DotIndicator(isActive: Boolean) {
    val color = if (isActive) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    }
    Box(
        modifier = Modifier
            .height(7.dp)
            .width(7.dp)
            .clip(CircleShape)
            .background(color)
    )
}