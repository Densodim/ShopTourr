package com.example.shoptourr.ui.purchase

import kotlin.test.Test
import kotlin.test.assertEquals

class ReceiptContentTypeTest {

    @Test
    fun `maps common image extensions`() {
        assertEquals("image/png", contentTypeForFileName("receipt.PNG"))
        assertEquals("image/webp", contentTypeForFileName("shot.webp"))
        assertEquals("image/heic", contentTypeForFileName("photo.heic"))
        assertEquals("image/jpeg", contentTypeForFileName("camera-capture.jpg"))
        assertEquals("image/jpeg", contentTypeForFileName("noext"))
    }
}
