package org.rocs.osda.mobile.ui.records

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.rocs.osda.mobile.ui.common.BackHeader
import org.rocs.osda.mobile.ui.common.OsdaCard
import org.rocs.osda.mobile.ui.common.PrimaryButton
import org.rocs.osda.mobile.ui.common.StatRow
import org.rocs.osda.mobile.ui.common.StatusColors
import org.rocs.osda.mobile.ui.common.StatusPill


@Composable
fun OffenseDetailScreen(
    viewModel: RecordsViewModel,
    onBack: () -> Unit,
    onFileAppeal: (recordId: Long) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val record = state.selectedRecord

    Column(modifier = Modifier.fillMaxSize()) {
        BackHeader("Offense Details", onBack)

        if (record == null) {
            Text(
                "No offense selected.",
                modifier = Modifier.padding(20.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@Column
        }

        Column(modifier = Modifier.fillMaxSize().weight(1f).padding(horizontal = 20.dp)) {
            val (fg, bg) = StatusColors.forRecord(record.status)
            OsdaCard(modifier = Modifier.padding(bottom = 16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "OFFENSE ID: OF-${record.recordId.toString().padStart(4, '0')}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    StatusPill(record.status.replaceFirstChar { it.uppercase() }, fg, bg)
                }
                Text(
                    record.offense.offense,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            OsdaCard(modifier = Modifier.padding(bottom = 16.dp)) {
                Text("Details", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                StatRow("Type", record.offense.type ?: "Not on file")
                Spacer(Modifier.height(8.dp))
                StatRow("Date of Violation", record.dateOfViolation)
                Spacer(Modifier.height(8.dp))
                StatRow("Reported By", record.employee?.fullName ?: "Not on file")
                Spacer(Modifier.height(8.dp))
                StatRow("Status", record.status.replaceFirstChar { it.uppercase() })
            }

            OsdaCard(modifier = Modifier.padding(bottom = 16.dp)) {
                Text("Disciplinary Action", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                Text(
                    record.action?.actionName ?: record.remarks ?: "No action recorded yet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
            val canAppeal = record.status.uppercase() == "PENDING"
            PrimaryButton(
                text = "File an Appeal",
                enabled = canAppeal,
                onClick = { onFileAppeal(record.recordId) }
            )
            if (!canAppeal) {
                Text(
                    "This offense is ${record.status.lowercase()} and can no longer be appealed.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        }
    }
}