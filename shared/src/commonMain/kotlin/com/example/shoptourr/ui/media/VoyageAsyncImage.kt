package com.example.shoptourr.ui.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.example.shoptourr.ui.privacy.SecureScreenCapture

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
    sensitive: Boolean = false,
) {
    if (sensitive) {
        SecureScreenCapture(enabled = true)
    }
    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
    )
}
