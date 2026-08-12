package com.example.shoptourr.ui.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.example.shoptourr.ui.components.VoyageButton
import com.example.shoptourr.ui.components.VoyageButtonVariant
import com.example.shoptourr.ui.components.VoyageEyebrow
import com.example.shoptourr.ui.components.VoyageScreen
import com.example.shoptourr.ui.i18n.t
import com.example.shoptourr.ui.testing.VoyageTestTags

@Composable
fun WelcomeScreen(
    onSignUp: () -> Unit,
    onSignIn: () -> Unit,
) {
    val title = t("welcome_title")
    val titleLines = title.split('\n')
    VoyageScreen {
        Spacer(Modifier.height(48.dp))
        VoyageEyebrow("Voyage")
        Spacer(Modifier.height(16.dp))
        Text(
            text = titleLines.firstOrNull().orEmpty(),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (titleLines.size > 1) {
            Text(
                text = titleLines.drop(1).joinToString("\n"),
                style = MaterialTheme.typography.displayLarge.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = t("welcome_sub"),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(40.dp))
        VoyageButton(
            text = t("create_account"),
            onClick = onSignUp,
            modifier = Modifier.testTag(VoyageTestTags.WELCOME_SIGN_UP),
        )
        Spacer(Modifier.height(12.dp))
        VoyageButton(
            text = t("have_account"),
            onClick = onSignIn,
            variant = VoyageButtonVariant.Secondary,
            modifier = Modifier.testTag(VoyageTestTags.WELCOME_SIGN_IN),
        )
    }
}
