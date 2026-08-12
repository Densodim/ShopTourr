package com.example.shoptourr.ui.legal

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shoptourr.ui.components.VoyageScreen
import com.example.shoptourr.ui.components.VoyageSurfaceBlock
import com.example.shoptourr.ui.components.VoyageTopBar
import com.example.shoptourr.ui.i18n.t

@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    LegalPage(
        title = t("privacy"),
        body = t("privacy_body"),
        onBack = onBack,
    )
}

@Composable
fun AboutScreen(onBack: () -> Unit) {
    LegalPage(
        title = t("about"),
        body = t("about_body"),
        onBack = onBack,
        footnote = "VOYAGE · v2.1.0",
    )
}

@Composable
fun SupportScreen(onBack: () -> Unit) {
    LegalPage(
        title = t("support"),
        body = t("support_body"),
        onBack = onBack,
    )
}

@Composable
private fun LegalPage(
    title: String,
    body: String,
    onBack: () -> Unit,
    footnote: String? = null,
) {
    VoyageScreen {
        VoyageTopBar(title = title, onBack = onBack)
        Spacer(Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(16.dp))
        VoyageSurfaceBlock {
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        if (footnote != null) {
            Spacer(Modifier.height(20.dp))
            Text(
                text = footnote,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
