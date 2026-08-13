package com.example.shoptourr.ui.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

/**
 * coil3 image pipeline for remote receipts / thumbnails.
 * Local receipt bytes still go through FileKit; this host is for HTTP URLs.
 */
@Composable
fun VoyageAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
    )
}
