package com.example.shoptourr.data.hash

import com.example.shoptourr.domain.hash.ContentChecksum

expect fun createDefaultContentChecksum(): ContentChecksum

expect fun sha256Digest(bytes: ByteArray): ByteArray
