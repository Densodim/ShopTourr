package com.example.shoptourr

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.browser.auth.AuthTabIntent
import com.example.shoptourr.data.auth.AndroidAuthHost
import com.example.shoptourr.data.auth.AndroidSocialAuthClient
import com.example.shoptourr.data.push.AndroidNotificationPermissionHost
import com.example.shoptourr.navigation.PendingDeepLinkStore
import com.example.shoptourr.navigation.VoyageDeepLinkRouter
import java.lang.ref.WeakReference
import org.koin.android.ext.android.inject

class MainActivity : FragmentActivity() {
    private val pendingDeepLinks: PendingDeepLinkStore by inject()

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            AndroidNotificationPermissionHost.complete(granted)
        }

    private val authTabLauncher = AuthTabIntent.registerActivityResultLauncher(this) { result ->
        when (result.resultCode) {
            AuthTabIntent.RESULT_OK -> AndroidSocialAuthClient.complete(result.resultUri?.toString())
            else -> AndroidSocialAuthClient.cancel()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // The app is light-only, so pin the bars to dark icons; the default `auto`
        // style follows the system and would put white icons on our paper background.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        bindAndroidHosts()
        offerDeepLinkFromIntent(intent)

        setContent {
            App()
        }
    }

    override fun onResume() {
        super.onResume()
        bindAndroidHosts()
    }

    override fun onDestroy() {
        if (AndroidAuthHost.currentActivity() === this) {
            AndroidAuthHost.activity = null
            AndroidAuthHost.authTabLauncher = null
            AndroidNotificationPermissionHost.launcher = null
        }
        super.onDestroy()
    }

    private fun bindAndroidHosts() {
        AndroidAuthHost.activity = WeakReference(this)
        AndroidAuthHost.authTabLauncher = authTabLauncher
        AndroidNotificationPermissionHost.launcher = { permission ->
            notificationPermissionLauncher.launch(permission)
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
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
