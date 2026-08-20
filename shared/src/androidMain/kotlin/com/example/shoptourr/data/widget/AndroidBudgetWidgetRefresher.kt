package com.example.shoptourr.data.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.example.shoptourr.domain.widget.BudgetWidgetRefresher

class AndroidBudgetWidgetRefresher(
    private val context: Context,
) : BudgetWidgetRefresher {
    override fun reload() {
        val app = context.applicationContext
        val component = ComponentName(
            app.packageName,
            RECEIVER,
        )
        val ids = AppWidgetManager.getInstance(app).getAppWidgetIds(component)
        if (ids.isEmpty()) return
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
            setComponent(component)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        app.sendBroadcast(intent)
    }

    private companion object {
        const val RECEIVER = "com.example.shoptourr.widget.BudgetGlanceWidgetReceiver"
    }
}
