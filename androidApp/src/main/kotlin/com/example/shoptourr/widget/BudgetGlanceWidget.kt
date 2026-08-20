package com.example.shoptourr.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.shoptourr.MainActivity
import com.example.shoptourr.domain.widget.BudgetWidgetContract
import com.example.shoptourr.domain.widget.BudgetWidgetCopy
import com.example.shoptourr.domain.widget.BudgetWidgetSnapshot

class BudgetGlanceWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = readSnapshot(context)
        provideContent {
            BudgetWidgetContent(snapshot)
        }
    }
}

class BudgetGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BudgetGlanceWidget()
}

private fun readSnapshot(context: Context): BudgetWidgetSnapshot {
    val prefs = context.getSharedPreferences(
        BudgetWidgetContract.PREFS_NAME,
        Context.MODE_PRIVATE,
    )
    return BudgetWidgetCopy.decode(prefs.getString(BudgetWidgetContract.JSON_KEY, null))
        ?: BudgetWidgetSnapshot(
            city = "ShopTourr",
            remainingLine = "",
        )
}

@Composable
private fun BudgetWidgetContent(snapshot: BudgetWidgetSnapshot) {
    val uri = snapshot.tripId
        ?.takeIf { it.isNotBlank() }
        ?.let { "voyage://trips/$it" }
        ?: "voyage://home"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
        setClassName("com.shoptourr", MainActivity::class.java.name)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val ink = ColorProvider(Color(0xFF1C1917))
    val muted = ColorProvider(Color(0xFF8A8177))
    val amount = if (snapshot.overBudget) ColorProvider(Color(0xFF9C3B28)) else ink
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFFFBF9F4))
            .padding(16.dp)
            .clickable(onClick = actionStartActivity(intent)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = snapshot.city.uppercase(),
            style = TextStyle(color = muted, fontSize = 11.sp, fontWeight = FontWeight.Medium),
        )
        if (snapshot.remainingLine.isNotBlank()) {
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = snapshot.remainingLine,
                style = TextStyle(color = amount, fontSize = 20.sp, fontWeight = FontWeight.Medium),
                maxLines = 2,
            )
        }
    }
}
