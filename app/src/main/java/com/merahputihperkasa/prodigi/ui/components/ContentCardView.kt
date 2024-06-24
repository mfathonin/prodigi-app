package com.merahputihperkasa.prodigi.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.merahputihperkasa.prodigi.models.Collection
import com.merahputihperkasa.prodigi.models.Content
import com.merahputihperkasa.prodigi.models.Link
import com.merahputihperkasa.prodigi.ui.theme.ProdigiBookReaderTheme
import com.merahputihperkasa.prodigi.ui.theme.Secondary400
import com.merahputihperkasa.prodigi.utils.openUrl

@Composable
fun ContentCardView(
    content: Content,
    onItemClick: () -> Unit = {},
    adaptiveColor: Boolean = true,
) {
    val context = LocalContext.current
    val link = content.link.targetUrl
    val title = content.title
    val collection = content.collection.name

    val textColor = if (adaptiveColor) {
        MaterialTheme.colorScheme.onSurface
    } else {
        Color.Black
    }

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Secondary400, RoundedCornerShape(12.dp))
            .clickable {
                openUrl(context, link)
                onItemClick.invoke()
            }
            .padding(16.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.bodyLarge
                .copy(fontWeight = FontWeight.Bold),
            color = textColor
        )
        Text(
            collection,
            style = MaterialTheme.typography.labelMedium,
            color = textColor.copy(0.8f),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ContentCardViewPreview() {
    val content = Content(
        id = "1",
        title = "Title",
        link = Link(
            url = "https://www.google.com",
            targetUrl = "https://www.youtube.com",
        ),
        collectionId = "1",
        collection = Collection(
            id = "1",
            name = "Collection",
            attributes = null
        )
    )

    ProdigiBookReaderTheme {
        ContentCardView(content)
    }
}