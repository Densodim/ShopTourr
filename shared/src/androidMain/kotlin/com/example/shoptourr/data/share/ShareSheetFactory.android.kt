package com.example.shoptourr.data.share

import android.content.Intent
import com.example.shoptourr.data.auth.AndroidAuthHost
import com.example.shoptourr.domain.share.ShareSheet

class AndroidShareSheet : ShareSheet {
    override fun shareText(text: String) {
        val activity = AndroidAuthHost.currentActivity() ?: return
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        activity.startActivity(Intent.createChooser(send, null))
    }
}

actual fun createShareSheet(): ShareSheet = AndroidShareSheet()
