package org.rocs.osda.mobile.ui.navigation

import android.annotation.SuppressLint
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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
import kotlin.math.abs
import kotlin.math.roundToInt

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

// Below this total movement (in px), a touch-and-release on the chat bubble
// is treated as a tap (fires onClick) rather than a drag (snaps to an edge).
private const val DRAG_CLICK_THRESHOLD_PX = 24f

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun OsdaNavHost(app: OsdaApplication, navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Box(modifier = Modifier.fillMaxSize()) {
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
                    factory = viewModelFactory {
                        initializer {
                            ChatViewModel(app.chatRepository, app.recordRepository, app.appealRepository, app.enrollmentRepository)
                        }
                    }
                )
                ChatScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onViewAppeals = { navController.navigate(Routes.appealsRoute()) { tabNavOptions(navController) } },
                    onViewOffenses = { navController.navigate(Routes.OFFENSES) { tabNavOptions(navController) } }
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
                                ProfileViewModel(app.sessionManager, app.enrollmentRepository, app.guardianRepository, app.recordRepository, app.appealRepository, app.themePreferences)
                            }
                        }
                    )
                    ProfileScreen(viewModel = viewModel)
                }
            }
        }

        // Free-floating chatbot entry point, similar to iOS's AssistiveTouch
        // bubble -- persists on top of every screen except Login (no session
        // yet) and Chat itself (already there), and can be dragged anywhere
        // on screen rather than being pinned to one corner. The drag state
        // lives in this always-composed BoxWithConstraints (not inside the
        // conditionally-shown bubble itself) so the position survives
        // hiding/showing as you navigate into and out of Chat.
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val density = LocalDensity.current
            val fabSizePx = with(density) { 56.dp.toPx() }
            val marginPx = with(density) { 16.dp.toPx() }

            val minX = marginPx
            val maxX = (with(density) { maxWidth.toPx() } - fabSizePx - marginPx).coerceAtLeast(minX)
            val minY = marginPx
            val maxY = (with(density) { maxHeight.toPx() } - fabSizePx - marginPx).coerceAtLeast(minY)

            // Held as MutableState objects (not unwrapped Float + callback)
            // so the drag handler below can read/write the *live* value at
            // any moment. An unwrapped value passed as a plain parameter
            // gets frozen at whatever it was when this gesture coroutine
            // was launched, which was the root cause of the FAB snapping
            // back to a stale position after dragging.
            val offsetX = remember { mutableStateOf(maxX) }
            val offsetY = remember {
                mutableStateOf((maxY - with(density) { 96.dp.toPx() }).coerceIn(minY, maxY))
            }

            if (currentRoute != null && currentRoute != Routes.LOGIN && currentRoute != Routes.CHAT) {
                DraggableChatFab(
                    offsetX = offsetX,
                    offsetY = offsetY,
                    minX = minX,
                    maxX = maxX,
                    minY = minY,
                    maxY = maxY,
                    onClick = { navController.navigate(Routes.CHAT) }
                )
            }
        }
    }
}

@Composable
private fun DraggableChatFab(
    offsetX: MutableState<Float>,
    offsetY: MutableState<Float>,
    minX: Float,
    maxX: Float,
    minY: Float,
    maxY: Float,
    onClick: () -> Unit
) {
    // Used only for the settle-to-edge animation after a drag, launched
    // *outside* the pointer-input gesture coroutine below. AwaitPointerEventScope
    // is a restricted-suspension scope: code inside awaitEachGesture can only
    // call suspend functions that belong to that scope itself (awaitPointerEvent,
    // etc.), not arbitrary suspend functions like Animatable.animateTo/snapTo or
    // the animate() helper -- that's what "Restricted suspending functions can
    // invoke member or extension suspending functions only on their restricted
    // receiver" was flagging when those were called directly inside the gesture.
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
            .size(56.dp)
            .pointerInput(minX, maxX, minY, maxY) {
                // Hand-rolled instead of detectDragGestures: that function
                // only calls onDragStart/onDragEnd once the touch has
                // already moved past the system touch-slop, so a plain tap
                // (down, no movement, up) never fires onDragEnd at all --
                // that was silently swallowing every tap on the bubble.
                // Tracking the gesture manually from the first down event
                // lets a small/no movement fall through to onClick().
                awaitEachGesture {
                    val down = awaitFirstDown()
                    var totalDrag = 0f

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break

                        val dragAmount = change.position - change.previousPosition
                        if (dragAmount.x != 0f || dragAmount.y != 0f) {
                            change.consume()
                            totalDrag += abs(dragAmount.x) + abs(dragAmount.y)
                            // Plain state writes, not suspend calls -- safe
                            // to make directly inside this restricted
                            // pointer-input coroutine, and always reflect
                            // the true live position the instant the loop
                            // below exits.
                            offsetX.value = (offsetX.value + dragAmount.x).coerceIn(minX, maxX)
                            offsetY.value = (offsetY.value + dragAmount.y).coerceIn(minY, maxY)
                        }
                    }

                    if (totalDrag < DRAG_CLICK_THRESHOLD_PX) {
                        onClick()
                    } else {
                        // Snap to whichever edge it's closest to, like
                        // iOS's AssistiveTouch bubble settling against the
                        // side of the screen when you let go. Animated in a
                        // separate, unrestricted coroutine (see the scope
                        // comment above).
                        val start = offsetX.value
                        val target = if (start < (minX + maxX) / 2) minX else maxX
                        scope.launch {
                            animate(
                                initialValue = start,
                                targetValue = target,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                            ) { value, _ -> offsetX.value = value }
                        }
                    }
                }
            }
            .shadow(elevation = 6.dp, shape = CircleShape)
            .background(color = OsdaTokens.blue, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.AutoAwesome,
            contentDescription = "Ask the Chatbot",
            tint = Color.White
        )
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
