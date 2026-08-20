package com.example.shoptourr.data.auth

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import java.lang.ref.WeakReference

object AndroidAuthHost {
    @Volatile
    var activity: WeakReference<Activity>? = null

    @Volatile
    var authTabLauncher: ActivityResultLauncher<Intent>? = null

    fun currentActivity(): Activity? = activity?.get()
}
