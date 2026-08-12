package com.example.shoptourr

import com.example.shoptourr.data.push.DevicePushTokenHolder
import com.example.shoptourr.navigation.PendingDeepLinkStore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.core.context.GlobalContext

class VoyageFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        DevicePushTokenHolder.update(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val store = runCatching {
            GlobalContext.get().get<PendingDeepLinkStore>()
        }.getOrNull() ?: return
        store.offerPushData(message.data)
    }
}
