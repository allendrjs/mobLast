package org.rocs.osda.mobile.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import org.rocs.osda.mobile.OsdaApplication
import org.rocs.osda.mobile.ui.appeal.AppealScreen
import org.rocs.osda.mobile.ui.appeal.AppealViewModel
import org.rocs.osda.mobile.ui.chat.ChatScreen
import org.rocs.osda.mobile.ui.chat.ChatViewModel
import org.rocs.osda.mobile.ui.dashboard.DashboardScreen
import org.rocs.osda.mobile.ui.dashboard.DashboardViewModel
import org.rocs.osda.mobile.ui.login.LoginScreen
import org.rocs.osda.mobile.ui.login.LoginViewModel
import org.rocs.osda.mobile.ui.profile.ProfileScreen
import org.rocs.osda.mobile.ui.profile.ProfileViewModel
import org.rocs.osda.mobile.ui.records.OffenseDetailScreen
import org.rocs.osda.mobile.ui.records.OffensesScreen
import org.rocs.osda.mobile.ui.records.RecordsViewModel

private object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val OFFENSES = "offenses"
    const val APPEALS = "appeals"
    const val PROFILE = "profile"
    const val CHAT = "chat"
    const val APPEAL_RECORD_ARG = "recordId"
    const val APPEALS_PATTERN = "$APPEALS?$APPEAL_RECORD_ARG={$APPEAL_RECORD_ARG}"

    fun appealsRoute(recordId: Long? = null): String =
        if (recordId != null) "$APPEALS?$APPEAL_RECORD_ARG=$recordId" else APPEALS
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
                    onFileAppeal = { navController.navigate(Routes.appealsRoute()) { tabNavOptions(navController) } },
                    onOpenChat = { navController.navigate(Routes.CHAT) }
                )
            }
        }

        composable(Routes.CHAT) {
            ChatScreen(
                viewModel = remember { ChatViewModel(app.chatRepository) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.OFFENSES) {
            val recordsViewModel = remember { RecordsViewModel(app.recordRepository) }
            val state by recordsViewModel.uiState.collectAsState()
            OsdaTabScaffold(navController, OsdaTab.OFFENSES, app) {
                if (state.selectedRecord == null) {
                    OffensesScreen(
                        viewModel = recordsViewModel,
                        onOpenOffense = { }
                    )
                } else {
                    OffenseDetailScreen(
                        viewModel = recordsViewModel,
                        onBack = { recordsViewModel.clearSelection() },
                        onFileAppeal = { recordId ->
                            recordsViewModel.clearSelection()
                            navController.navigate(Routes.appealsRoute(recordId)) { tabNavOptions(navController) }
                        }
                    )
                }
            }
        }

        composable(
            route = Routes.APPEALS_PATTERN,
            arguments = listOf(navArgument(Routes.APPEAL_RECORD_ARG) {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getLong(Routes.APPEAL_RECORD_ARG)?.takeIf { it > 0 }
            OsdaTabScaffold(navController, OsdaTab.APPEALS, app) {
                AppealScreen(
                    viewModel = remember(recordId) {
                        AppealViewModel(app.appealRepository, app.recordRepository, app.enrollmentRepository, recordId)
                    }
                )
            }
        }

        composable(Routes.PROFILE) {
            OsdaTabScaffold(navController, OsdaTab.PROFILE, app) {
                ProfileScreen(
                    viewModel = remember {
                        ProfileViewModel(app.sessionManager, app.enrollmentRepository, app.guardianRepository, app.recordRepository, app.appealRepository)
                    }
                )
            }
        }
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
                    OsdaTab.APPEALS -> navController.navigate(Routes.appealsRoute()) { tabNavOptions(navController) }
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