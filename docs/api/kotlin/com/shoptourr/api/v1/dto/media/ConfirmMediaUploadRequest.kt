package com.shoptourr.api.v1.dto.media

data class ConfirmMediaUploadRequest(
    /** Optional; server can also detect via storage event. */
    val uploaded: Boolean = false,
)
