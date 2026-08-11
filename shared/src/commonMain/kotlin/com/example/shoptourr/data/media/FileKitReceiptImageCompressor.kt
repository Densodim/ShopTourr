package com.example.shoptourr.data.media

import com.example.shoptourr.domain.media.CompressedReceipt
import com.example.shoptourr.domain.media.ReceiptImageCompressor
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.ImageFormat
import io.github.vinceglb.filekit.compressImage

/**
 * Downscales and JPEG-encodes receipt photos before pre-signed upload.
 */
class FileKitReceiptImageCompressor(
    private val quality: Int = 80,
    private val maxWidth: Int = 1600,
    private val maxHeight: Int = 1600,
    private val minBytesToCompress: Int = 200_000,
) : ReceiptImageCompressor {
    override suspend fun compress(bytes: ByteArray, contentType: String): CompressedReceipt {
        if (bytes.size < minBytesToCompress) {
            return CompressedReceipt(bytes = bytes, contentType = contentType)
        }
        val compressed = FileKit.compressImage(
            bytes = bytes,
            quality = quality,
            maxWidth = maxWidth,
            maxHeight = maxHeight,
            imageFormat = ImageFormat.JPEG,
        )
        return CompressedReceipt(bytes = compressed, contentType = "image/jpeg")
    }
}
