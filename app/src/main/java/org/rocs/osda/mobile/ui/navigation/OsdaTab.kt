package org.rocs.osda.mobile.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

enum class OsdaTab(val route: String, val label: String, val icon: ImageVector) {
    DASHBOARD("dashboard", "Dashboard", Icons.Filled.Dashboard),
    OFFENSES("offenses", "Offenses", Icons.Filled.Assignment),
    APPEALS("appeals", "Appeal", Icons.Filled.Gavel),
    PROFILE("profile", "Profile", Icons.Filled.Person),
    LOGOUT("logout", "Logout", Icons.AutoMirrored.Filled.Logout)
}
