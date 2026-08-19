package com.example.shoptourr.data.media

interface UploadCheckpointStore {
    fun offsetBytes(mediaId: String): Long
    fun save(mediaId: String, offsetBytes: Long)
    fun clear(mediaId: String)
}

class InMemoryUploadCheckpointStore : UploadCheckpointStore {
    private val offsets = mutableMapOf<String, Long>()

    override fun offsetBytes(mediaId: String): Long = offsets[mediaId] ?: 0L

    override fun save(mediaId: String, offsetBytes: Long) {
        offsets[mediaId] = offsetBytes.coerceAtLeast(0L)
    }

    override fun clear(mediaId: String) {
        offsets.remove(mediaId)
    }
}
