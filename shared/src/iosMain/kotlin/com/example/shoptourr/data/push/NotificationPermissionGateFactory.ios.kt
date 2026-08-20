package com.example.shoptourr.data.push

import com.example.shoptourr.domain.push.NotificationPermissionGate

actual fun createNotificationPermissionGate(): NotificationPermissionGate =
    IosNotificationPermissionGate()
