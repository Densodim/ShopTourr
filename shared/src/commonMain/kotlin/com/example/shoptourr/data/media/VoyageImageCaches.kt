package com.example.shoptourr.data.media

import coil3.ImageLoader
import kotlin.concurrent.Volatile

/** Process-wide Coil handle so logout can drop receipt thumbnails from disk/memory. */
object VoyageImageCaches {
    @Volatile
    var loader: ImageLoader? = null

    fun clear() {
        val imageLoader = loader ?: return
        imageLoader.memoryCache?.clear()
        imageLoader.diskCache?.clear()
    }
}
