package com.example.shoptourr.ui.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.example.shoptourr.ui.components.VoyageButton
import com.example.shoptourr.ui.components.VoyageButtonVariant
import com.example.shoptourr.ui.components.VoyageEyebrow
import com.example.shoptourr.ui.components.VoyageScreen

@Composable
fun WelcomeScreen(
    onSignUp: () -> Unit,
    onSignIn: () -> Unit,
) {
    VoyageScreen {
        Spacer(Modifier.height(48.dp))
        VoyageEyebrow("Voyage")
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Путешествуй.\nСчитай. Вспоминай.",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Трекер поездок, VAT и Tax Free — в одном кармане.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontStyle = FontStyle.Italic,
        )
        Spacer(Modifier.height(40.dp))
        VoyageButton(text = "Создать аккаунт", onClick = onSignUp)
        Spacer(Modifier.height(12.dp))
        VoyageButton(
            text = "Войти",
            onClick = onSignIn,
            variant = VoyageButtonVariant.Secondary,
        )
    }
}
