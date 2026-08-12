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
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = OsdaTokens.navInactive,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = OsdaTokens.navInactive,
                    indicatorColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}
