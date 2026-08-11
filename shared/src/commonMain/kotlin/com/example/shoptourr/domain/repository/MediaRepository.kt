package com.example.shoptourr.domain.repository

import com.example.shoptourr.domain.model.MediaAsset
import com.example.shoptourr.domain.model.MediaUploadIntent
import com.example.shoptourr.domain.model.ReceiptOcrResult
import com.example.shoptourr.domain.model.ReceiptUploadDraft

interface MediaRepository {
    suspend fun createReceiptUploadIntent(draft: ReceiptUploadDraft): Result<MediaUploadIntent>
    suspend fun uploadBytes(intent: MediaUploadIntent, bytes: ByteArray): Result<Unit>
    suspend fun confirmUpload(mediaId: String): Result<MediaAsset>
    suspend fun getAsset(mediaId: String): Result<MediaAsset>
    suspend fun getOcr(mediaId: String): Result<ReceiptOcrResult>
}
