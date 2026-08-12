package com.example.shoptourr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.shoptourr.ui.i18n.t
import com.example.shoptourr.ui.navigation.VoyageTab
import com.example.shoptourr.ui.theme.VoyageTokens

@Composable
fun VoyageTabBar(
    current: VoyageTab,
    onChange: (VoyageTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(VoyageTokens.surface.copy(alpha = 0.96f))
            .border(1.dp, VoyageTokens.border)
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VoyageTab.entries.forEach { tab ->
            val selected = tab == current
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { onChange(tab) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = tabGlyph(tab),
                    color = if (selected) VoyageTokens.accent else VoyageTokens.muted,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = tabLabel(tab),
                    color = if (selected) VoyageTokens.ink else VoyageTokens.muted,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun tabLabel(tab: VoyageTab): String = when (tab) {
    VoyageTab.Home -> t("tab_home")
    VoyageTab.Wishlist -> t("tab_wishlist")
    VoyageTab.Profile -> t("tab_profile")
}

private fun tabGlyph(tab: VoyageTab): String = when (tab) {
    VoyageTab.Home -> "⌂"
    VoyageTab.Wishlist -> "♡"
    VoyageTab.Profile -> "◉"
}
