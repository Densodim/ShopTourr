package com.example.shoptourr

import com.example.shoptourr.data.push.DevicePushTokenHolder
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class VoyageFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        DevicePushTokenHolder.update(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        // Display handled later; token registration is the v1 goal.
    }
}
