package com.example.shoptourr

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.shoptourr.navigation.PendingDeepLinkStore
import com.example.shoptourr.navigation.VoyageDeepLinkRouter
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val pendingDeepLinks: PendingDeepLinkStore by inject()

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* best-effort */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        // The app is light-only, so pin the bars to dark icons; the default `auto`
        // style follows the system and would put white icons on our paper background.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()
        offerDeepLinkFromIntent(intent)

        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        offerDeepLinkFromIntent(intent)
    }

    private fun offerDeepLinkFromIntent(intent: Intent?) {
        if (intent == null) return
        val extras = buildMap {
            intent.extras?.keySet()?.forEach { key ->
                intent.extras?.getString(key)?.let { put(key, it) }
            }
        }
        if (extras.isNotEmpty()) {
            pendingDeepLinks.offerPushData(extras)
        }
        intent.dataString?.let { uri ->
            VoyageDeepLinkRouter.resolveUri(uri)?.let(pendingDeepLinks::offer)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
