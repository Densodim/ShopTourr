package com.example.shoptourr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.shoptourr.ui.i18n.t
import com.example.shoptourr.ui.navigation.VoyageTab
import com.example.shoptourr.ui.testing.VoyageTestTags
import com.example.shoptourr.ui.theme.VoyageTokens

@Composable
fun VoyageTabBar(
    current: VoyageTab,
    onChange: (VoyageTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    // `.tab-bar` sits on bare paper; each tab is topped by a rule that goes
    // oxblood on the active one, so together they read as one hairline.
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(VoyageTokens.bg)
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 14.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VoyageTab.entries.forEach { tab ->
            val selected = tab == current
            val label = tabLabel(tab)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .testTag(tabTestTag(tab))
                    .semantics { contentDescription = label }
                    .clickable { onChange(tab) },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.5.dp)
                        .background(if (selected) VoyageTokens.accent else VoyageTokens.border),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = tabGlyph(tab),
                    color = if (selected) VoyageTokens.ink else VoyageTokens.muted,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = label.uppercase(),
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

private fun tabTestTag(tab: VoyageTab): String = when (tab) {
    VoyageTab.Home -> VoyageTestTags.TAB_HOME
    VoyageTab.Wishlist -> VoyageTestTags.TAB_WISHLIST
    VoyageTab.Profile -> VoyageTestTags.TAB_PROFILE
}
