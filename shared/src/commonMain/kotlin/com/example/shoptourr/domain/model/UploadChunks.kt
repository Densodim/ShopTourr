package com.example.shoptourr.domain.model

object UploadChunks {
    const val DEFAULT_SIZE = 256 * 1024

    fun slice(bytes: ByteArray, offset: Long, chunkSize: Int = DEFAULT_SIZE): ByteArray {
        if (bytes.isEmpty() || chunkSize <= 0) return byteArrayOf()
        val start = offset.coerceIn(0L, bytes.size.toLong()).toInt()
        val end = (start.toLong() + chunkSize.toLong()).coerceAtMost(bytes.size.toLong()).toInt()
        if (start >= end) return byteArrayOf()
        return bytes.copyOfRange(start, end)
    }
}

object UploadResumeOffset {
    fun resolve(checkpoint: Long, serverOffset: Long?): Long {
        if (serverOffset != null) return serverOffset.coerceAtLeast(0L)
        return checkpoint.coerceAtLeast(0L)
    }
}
