package org.rocs.osda.mobile.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.rocs.osda.mobile.ui.common.OsdaCard
import org.rocs.osda.mobile.ui.common.StatCard
import org.rocs.osda.mobile.ui.common.StatusPill
import org.rocs.osda.mobile.ui.theme.OsdaTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onViewOffenses: () -> Unit,
    onFileAppeal: () -> Unit,
    onOpenChat: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val enrollment = state.enrollment
    val displayName = enrollment?.student?.person?.fullName ?: state.studentId ?: "Student"
    val program = listOfNotNull(enrollment?.student?.studentType, enrollment?.section)
        .joinToString(" ")
        .ifBlank { "No enrollment on file" }

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = viewModel::load,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            Column(modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                        .padding(18.dp)
                ) {
                    Text("Welcome back, $displayName", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "$program  •  Student ID ${state.studentId ?: "—"}",
                        color = OsdaTokens.primaryMuted,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                    )
                    val statusText = enrollment?.disciplinaryStatus?.status
                    if (statusText != null) {
                        val isGood = statusText.contains("good", ignoreCase = true)
                        StatusPill(statusText, if (isGood) OsdaTokens.green else OsdaTokens.amber, if (isGood) OsdaTokens.greenBg else OsdaTokens.amberBg)
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
            ) {
                StatCard(state.violationsCount.toString(), "Offenses", MaterialTheme.colorScheme.onBackground, Modifier.weight(1f))
                StatCard(state.pendingAppealsCount.toString(), "Pending Appeals", OsdaTokens.amber, Modifier.weight(1f))
                StatCard(enrollment?.studentLevel ?: "—", "Year/Grade", MaterialTheme.colorScheme.onBackground, Modifier.weight(1f))
            }

            Text("Quick Actions", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickActionRow("View Offenses", "See your disciplinary records", onClick = onViewOffenses)
                QuickActionRow("File an Appeal", "Request a review of a case", onClick = onFileAppeal)
                QuickActionRow("Ask the Chatbot", "Get answers about your offenses and appeals", onClick = onOpenChat)
            }

            Text("Recent Activity", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))
            if (state.recentActivity.isEmpty()) {
                Text("Nothing to show yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.recentActivity.forEach { item -> ActivityRow(item) }
                }
            }
        }
    }
}

@Composable
private fun QuickActionRow(title: String, subtitle: String, enabled: Boolean = true, onClick: () -> Unit) {
    val alpha = if (enabled) 1f else 0.5f
    OsdaCard(modifier = Modifier.clickable(enabled = enabled, onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = alpha))
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha), style = MaterialTheme.typography.labelSmall)
            }
            Text("›", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ActivityRow(item: ActivityItem) {
    val (title, subtitle) = when (item) {
        is ActivityItem.RecordLogged -> "New Offense Logged" to "${item.record.offense.offense}  •  ${item.record.dateOfViolation}"
        is ActivityItem.AppealUpdated -> "Appeal ${item.appeal.status.lowercase().replaceFirstChar { it.uppercase() }}" to
                "${item.appeal.record?.offense?.offense ?: "Offense"}  •  ${item.appeal.dateProcessed ?: item.appeal.dateFiled ?: ""}"
    }
    OsdaCard {
        Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 2.dp), )
    }
}