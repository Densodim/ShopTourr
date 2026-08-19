package com.example.shoptourr

import com.example.shoptourr.observability.Observability
import io.sentry.Sentry
import io.sentry.SentryLevel
import io.sentry.android.core.SentryAndroid

class AndroidSentryObservability : Observability {
    override fun captureException(throwable: Throwable, extras: Map<String, String>) {
        Sentry.withScope { scope ->
            extras.forEach { (key, value) -> scope.setExtra(key, value) }
            Sentry.captureException(throwable)
        }
    }

    override fun addBreadcrumb(
        message: String,
        category: String,
        data: Map<String, String>,
    ) {
        val extras = data
        val crumb = io.sentry.Breadcrumb().apply {
            this.message = message
            this.category = category
            this.level = SentryLevel.INFO
            extras.forEach { (key, value) -> setData(key, value) }
        }
        Sentry.addBreadcrumb(crumb)
    }

    override fun setTag(key: String, value: String) {
        Sentry.setTag(key, value)
    }
}

fun initSentryIfConfigured(app: android.app.Application, dsn: String?, debug: Boolean) {
    if (dsn.isNullOrBlank()) return
    SentryAndroid.init(app) { options ->
        options.dsn = dsn
        options.isDebug = debug
    }
}
