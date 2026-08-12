package org.rocs.osda.mobile.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

enum class OsdaTab(val route: String, val label: String, val icon: ImageVector) {
    PROFILE("profile", "Profile", Icons.Filled.Person),
    OFFENSES("offenses", "Offenses", Icons.Filled.Assignment),
    DASHBOARD("dashboard", "Dashboard", Icons.Filled.Dashboard),
    APPEALS("appeals", "Appeal", Icons.Filled.Gavel),
    LOGOUT("logout", "Logout", Icons.AutoMirrored.Filled.Logout)
}
