package org.rocs.osda.mobile.ui.appeal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.rocs.osda.mobile.data.model.Appeal
import org.rocs.osda.mobile.ui.common.OsdaCard
import org.rocs.osda.mobile.ui.common.PrimaryButton
import org.rocs.osda.mobile.ui.common.FilterPill
import org.rocs.osda.mobile.ui.common.StatCard
import org.rocs.osda.mobile.ui.common.StatusColors
import org.rocs.osda.mobile.ui.common.StatusPill
import org.rocs.osda.mobile.ui.theme.OsdaTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppealScreen(viewModel: AppealViewModel) {
    val state by viewModel.uiState.collectAsState()

    if (viewModel.isFilingMode) {
        FileAppealContent(viewModel)
    } else {
        MyAppealsContent(viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FileAppealContent(viewModel: AppealViewModel) {
    val state by viewModel.uiState.collectAsState()
    val offenseRecord = state.records.firstOrNull { it.recordId == state.selectedRecordId }

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = viewModel::load,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            Column(modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)) {
                Text("File a New Appeal", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "Request a review of an offense on your record",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OsdaCard(modifier = Modifier.padding(bottom = 20.dp)) {
                Text("Offense", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 8.dp))
                Text(
                    if (offenseRecord != null) "${offenseRecord.offense.offense} • ${offenseRecord.dateOfViolation}" else "Offense not found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text("Message", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                OutlinedTextField(
                    value = state.message,
                    onValueChange = viewModel::onMessageChange,
                    placeholder = { Text("Explain why you're appealing this offense...") },
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                )

                state.submitError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                }
                if (state.submitSuccess) {
                    Text("Appeal submitted successfully.", color = OsdaTokens.green, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                }

                Spacer(Modifier.height(16.dp))
                PrimaryButton(
                    text = if (state.isSubmitting) "Submitting..." else "Submit Appeal",
                    enabled = !state.isSubmitting && !state.submitSuccess,
                    onClick = viewModel::submit
                )
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyAppealsContent(viewModel: AppealViewModel) {
    val state by viewModel.uiState.collectAsState()

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = viewModel::load,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            Column(modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)) {
                Text("My Appeals", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "Create and file offense appeals",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                StatCard(state.totalCount.toString(), "Total Filed", MaterialTheme.colorScheme.onBackground, Modifier.weight(1f))
                StatCard(state.pendingCount.toString(), "Pending", OsdaTokens.amber, Modifier.weight(1f))
                StatCard(state.approvedCount.toString(), "Approved", OsdaTokens.green, Modifier.weight(1f))
                StatCard(state.deniedCount.toString(), "Denied", OsdaTokens.red, Modifier.weight(1f))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                FilterPill("All", state.filter == AppealFilter.ALL) { viewModel.setFilter(AppealFilter.ALL) }
                FilterPill("Pending", state.filter == AppealFilter.PENDING) { viewModel.setFilter(AppealFilter.PENDING) }
                FilterPill("Approved", state.filter == AppealFilter.APPROVED) { viewModel.setFilter(AppealFilter.APPROVED) }
                FilterPill("Denied", state.filter == AppealFilter.DENIED) { viewModel.setFilter(AppealFilter.DENIED) }
            }

            when {
                state.isLoading && state.appeals.isEmpty() -> Text("Loading...")
                state.error != null -> Text(state.error ?: "Something went wrong.", color = MaterialTheme.colorScheme.error)
                state.filteredAppeals.isEmpty() -> Text(
                    "No appeals to show.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.filteredAppeals) { appeal -> AppealHistoryCard(appeal) }
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun AppealHistoryCard(appeal: Appeal) {
    val (fg, bg) = StatusColors.forAppeal(appeal.status)
    OsdaCard {
        Text(
            "APPEAL ID: AP-${appeal.appealId.toString().padStart(4, '0')}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelSmall
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                appeal.record?.offense?.offense ?: "Offense",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )
            StatusPill(appeal.status.replaceFirstChar { it.uppercase() }, fg, bg)
        }
        Text(
            "Reason:",
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            appeal.message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 2.dp)
        )
        Text(
            "Submitted ${appeal.dateFiled ?: "—"}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 6.dp)
        )
        appeal.remarks?.let {
            Text(
                "Remarks: $it",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
