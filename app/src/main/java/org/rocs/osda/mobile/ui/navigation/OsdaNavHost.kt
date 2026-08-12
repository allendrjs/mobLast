package org.rocs.osda.mobile.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.rocs.osda.mobile.OsdaApplication
import org.rocs.osda.mobile.ui.dashboard.DashboardScreen
import org.rocs.osda.mobile.ui.dashboard.DashboardViewModel
import org.rocs.osda.mobile.ui.login.LoginScreen
import org.rocs.osda.mobile.ui.login.LoginViewModel

private object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val OFFENSES = "offenses"
    const val APPEALS = "appeals"
    const val PROFILE = "profile"
}

@Composable
fun OsdaNavHost(app: OsdaApplication, navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = remember { LoginViewModel(app.authRepository) },
                onLoginSuccess = {
                    navController.navigate(Routes.DASHBOARD) { popUpTo(Routes.LOGIN) { inclusive = true } }
                }
            )
        }

        composable(Routes.DASHBOARD) {
            OsdaTabScaffold(navController, OsdaTab.DASHBOARD, app) {
                DashboardScreen(
                    viewModel = remember {
                        DashboardViewModel(app.sessionManager, app.enrollmentRepository, app.recordRepository, app.appealRepository)
                    },
                    onViewOffenses = { navController.navigate(Routes.OFFENSES) { tabNavOptions(navController) } },
                    onFileAppeal = { navController.navigate(Routes.APPEALS) { tabNavOptions(navController) } }
                )
            }
        }

        composable(Routes.OFFENSES) {
            OsdaTabScaffold(navController, OsdaTab.OFFENSES, app) { ComingSoon("Offenses") }
        }

        composable(Routes.APPEALS) {
            OsdaTabScaffold(navController, OsdaTab.APPEALS, app) { ComingSoon("Appeals") }
        }

        composable(Routes.PROFILE) {
            OsdaTabScaffold(navController, OsdaTab.PROFILE, app) { ComingSoon("Profile") }
        }
    }
}

@Composable
private fun ComingSoon(label: String) {
    Box(modifier = Modifier.padding(24.dp)) {
        Text("$label - coming soon", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun OsdaTabScaffold(
    navController: NavHostController,
    currentTab: OsdaTab,
    app: OsdaApplication,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    Scaffold(
        bottomBar = {
            OsdaBottomBar(currentTab = currentTab) { tab ->
                when (tab) {
                    OsdaTab.DASHBOARD -> navController.navigate(Routes.DASHBOARD) { tabNavOptions(navController) }
                    OsdaTab.OFFENSES -> navController.navigate(Routes.OFFENSES) { tabNavOptions(navController) }
                    OsdaTab.APPEALS -> navController.navigate(Routes.APPEALS) { tabNavOptions(navController) }
                    OsdaTab.PROFILE -> navController.navigate(Routes.PROFILE) { tabNavOptions(navController) }
                    OsdaTab.LOGOUT -> scope.launch {
                        app.sessionManager.clear()
                        navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}

private fun NavOptionsBuilder.tabNavOptions(navController: NavHostController) {
    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}
