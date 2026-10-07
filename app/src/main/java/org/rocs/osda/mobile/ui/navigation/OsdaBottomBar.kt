package org.rocs.osda.mobile.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.rocs.osda.mobile.ui.theme.OsdaTokens

@Composable
fun OsdaBottomBar(currentTab: OsdaTab, onTabSelected: (OsdaTab) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        OsdaTab.values().forEach { tab ->
            val selected = tab == currentTab
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = { Icon(imageVector = tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    // OsdaTokens.blue instead of colorScheme.primary: primary
                    // is a constant dark navy (by design, since it's normally
                    // used as a solid-fill background elsewhere), which reads
                    // fine as text on a light bar but is nearly invisible as
                    // the *selected-tab* tint against a dark bar in dark mode.
                    // OsdaTokens.blue is dark-mode-aware and stays legible in
                    // both themes.
                    selectedIconColor = OsdaTokens.blue,
                    unselectedIconColor = OsdaTokens.navInactive,
                    selectedTextColor = OsdaTokens.blue,
                    unselectedTextColor = OsdaTokens.navInactive,
                    indicatorColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}
