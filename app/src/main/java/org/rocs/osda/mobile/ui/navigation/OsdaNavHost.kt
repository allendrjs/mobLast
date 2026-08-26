package org.rocs.osda.mobile.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
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
import org.rocs.osda.mobile.ui.theme.OsdaTokens
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
        composable(Routes.LOGIN) { backStackEntry ->
            val viewModel: LoginViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                factory = viewModelFactory { initializer { LoginViewModel(app.authRepository) } }
            )
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.DASHBOARD) { popUpTo(Routes.LOGIN) { inclusive = true } }
                }
            )
        }

        composable(Routes.DASHBOARD) { backStackEntry ->
            OsdaTabScaffold(navController, OsdaTab.DASHBOARD, app) {
                val viewModel: DashboardViewModel = viewModel(
                    viewModelStoreOwner = backStackEntry,
                    factory = viewModelFactory {
                        initializer {
                            DashboardViewModel(app.sessionManager, app.enrollmentRepository, app.recordRepository, app.appealRepository)
                        }
                    }
                )
                DashboardScreen(
                    viewModel = viewModel,
                    onViewOffenses = { navController.navigate(Routes.OFFENSES) { tabNavOptions(navController) } },
                    onFileAppeal = { navController.navigate(Routes.appealsRoute()) { tabNavOptions(navController) } },
                    onOpenChat = { navController.navigate(Routes.CHAT) }
                )
            }
        }

        composable(Routes.CHAT) { backStackEntry ->
            val viewModel: ChatViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                factory = viewModelFactory { initializer { ChatViewModel(app.chatRepository) } }
            )
            ChatScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.OFFENSES) { backStackEntry ->
            val recordsViewModel: RecordsViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                factory = viewModelFactory { initializer { RecordsViewModel(app.recordRepository, app.appealRepository) } }
            )
            val state by recordsViewModel.uiState.collectAsState()
            OsdaTabScaffold(navController, OsdaTab.OFFENSES, app) {
                if (state.selectedRecord == null) {
                    OffensesScreen(viewModel = recordsViewModel)
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
                // Keyed on recordId so navigating between different offenses' appeal
                // screens (which reuses this back stack entry under launchSingleTop)
                // still gets a fresh ViewModel instead of a stale cached one.
                val viewModel: AppealViewModel = viewModel(
                    viewModelStoreOwner = backStackEntry,
                    key = "appeal-$recordId",
                    factory = viewModelFactory {
                        initializer { AppealViewModel(app.appealRepository, app.recordRepository, app.enrollmentRepository, recordId) }
                    }
                )
                AppealScreen(viewModel = viewModel)
            }
        }

        composable(Routes.PROFILE) { backStackEntry ->
            OsdaTabScaffold(navController, OsdaTab.PROFILE, app) {
                val viewModel: ProfileViewModel = viewModel(
                    viewModelStoreOwner = backStackEntry,
                    factory = viewModelFactory {
                        initializer {
                            ProfileViewModel(app.sessionManager, app.enrollmentRepository, app.guardianRepository, app.recordRepository, app.appealRepository)
                        }
                    }
                )
                ProfileScreen(viewModel = viewModel)
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
    var showLogoutConfirm by remember { mutableStateOf(false) }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Log out?") },
            text = { Text("You'll need to sign in again to access your records.") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirm = false
                    scope.launch {
                        app.sessionManager.clear()
                        navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                    }
                }) { Text("Log Out") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        bottomBar = {
            OsdaBottomBar(currentTab = currentTab) { tab ->
                when (tab) {
                    OsdaTab.DASHBOARD -> navController.navigate(Routes.DASHBOARD) { tabNavOptions(navController) }
                    OsdaTab.OFFENSES -> navController.navigate(Routes.OFFENSES) { tabNavOptions(navController) }
                    OsdaTab.APPEALS -> navController.navigate(Routes.appealsRoute()) { tabNavOptions(navController) }
                    OsdaTab.PROFILE -> navController.navigate(Routes.PROFILE) { tabNavOptions(navController) }
                    OsdaTab.LOGOUT -> showLogoutConfirm = true
                }
            }
        },
        floatingActionButton = {
            // Floating chatbot entry point, visible across every tab -- same
            // idea as Meta AI's floating bubble in Messenger: always
            // reachable, distinct gradient color so it stands out from the
            // rest of the (mostly monochrome) app UI.
            FloatingActionButton(
                onClick = { navController.navigate(Routes.CHAT) },
                shape = CircleShape,
                containerColor = Color.Transparent,
                elevation = FloatingActionButtonDefaults.elevation(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(OsdaTokens.chatGradientStart, OsdaTokens.chatGradientEnd)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "Ask the Chatbot",
                        tint = Color.White
                    )
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
