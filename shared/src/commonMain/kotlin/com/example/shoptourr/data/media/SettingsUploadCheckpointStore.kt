package com.example.shoptourr.data.media

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

class SettingsUploadCheckpointStore(
    private val settings: Settings,
) : UploadCheckpointStore {
    override fun offsetBytes(mediaId: String): Long =
        settings.getLongOrNull(offsetKey(mediaId)) ?: 0L

    override fun save(mediaId: String, offsetBytes: Long) {
        settings[offsetKey(mediaId)] = offsetBytes.coerceAtLeast(0L)
        val ids = ids()
        if (ids.add(mediaId)) {
            writeIds(ids)
        }
    }

    override fun clear(mediaId: String) {
        settings.remove(offsetKey(mediaId))
        val ids = ids()
        if (ids.remove(mediaId)) {
            writeIds(ids)
        }
    }

    override fun clearAll() {
        ids().forEach { settings.remove(offsetKey(it)) }
        settings.remove(INDEX)
    }

    private fun ids(): MutableSet<String> {
        val raw = settings.getStringOrNull(INDEX) ?: return mutableSetOf()
        return raw.split('\n').filter { it.isNotEmpty() }.toMutableSet()
    }

    private fun writeIds(ids: Set<String>) {
        if (ids.isEmpty()) {
            settings.remove(INDEX)
        } else {
            settings[INDEX] = ids.joinToString("\n")
        }
    }

    private fun offsetKey(mediaId: String) = "$PREFIX$mediaId"

    private companion object {
        const val PREFIX = "upload.checkpoint."
        const val INDEX = "upload.checkpoint.ids"
    }
}
