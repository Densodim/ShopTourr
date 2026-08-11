package com.example.shoptourr.domain.media

data class CompressedReceipt(
    val bytes: ByteArray,
    val contentType: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as CompressedReceipt
        return contentType == other.contentType && bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        var result = contentType.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}

fun interface ReceiptImageCompressor {
    suspend fun compress(bytes: ByteArray, contentType: String): CompressedReceipt
}

object PassthroughReceiptImageCompressor : ReceiptImageCompressor {
    override suspend fun compress(bytes: ByteArray, contentType: String): CompressedReceipt =
        CompressedReceipt(bytes = bytes, contentType = contentType)
}
