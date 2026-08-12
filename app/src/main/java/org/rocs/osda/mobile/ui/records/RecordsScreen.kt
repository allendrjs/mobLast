package org.rocs.osda.mobile.ui.records

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.rocs.osda.mobile.data.model.OffenseRecord
import org.rocs.osda.mobile.ui.common.FilterPill
import org.rocs.osda.mobile.ui.common.OsdaCard
import org.rocs.osda.mobile.ui.common.StatCard
import org.rocs.osda.mobile.ui.common.StatusColors
import org.rocs.osda.mobile.ui.common.StatusPill
import org.rocs.osda.mobile.ui.theme.OsdaTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffensesScreen(
    viewModel: RecordsViewModel,
    onOpenOffense: (OffenseRecord) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = viewModel::load,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            Column(modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)) {
                Text("My Disciplinary Records", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "Track and view all logged offenses",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                StatCard(state.totalCount.toString(), "Total", MaterialTheme.colorScheme.onBackground, Modifier.weight(1f))
                StatCard(state.activeCount.toString(), "Active", OsdaTokens.amber, Modifier.weight(1f))
                StatCard(state.resolvedCount.toString(), "Resolved", OsdaTokens.green, Modifier.weight(1f))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                FilterPill("All", state.filter == OffenseFilter.ALL) { viewModel.setFilter(OffenseFilter.ALL) }
                FilterPill("Active", state.filter == OffenseFilter.ACTIVE) { viewModel.setFilter(OffenseFilter.ACTIVE) }
                FilterPill("Resolved", state.filter == OffenseFilter.RESOLVED) { viewModel.setFilter(OffenseFilter.RESOLVED) }
            }

            when {
                state.isLoading && state.records.isEmpty() -> Text("Loading...")
                state.error != null -> Text(state.error ?: "Something went wrong.", color = MaterialTheme.colorScheme.error)
                state.filteredRecords.isEmpty() -> Text(
                    "No offenses on file.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.filteredRecords) { record ->
                        OffenseCard(record) {
                            viewModel.selectRecord(record)
                            onOpenOffense(record)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OffenseCard(record: OffenseRecord, onClick: () -> Unit) {
    val (fg, bg) = StatusColors.forRecord(record.status)
    OsdaCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "OFFENSE ID: OF-${record.recordId.toString().padStart(4, '0')}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelSmall
            )
            StatusPill(record.status.replaceFirstChar { it.uppercase() }, fg, bg)
        }
        Text(
            record.offense.offense,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 6.dp)
        )
        Text(
            "Filed: ${record.dateOfViolation}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}