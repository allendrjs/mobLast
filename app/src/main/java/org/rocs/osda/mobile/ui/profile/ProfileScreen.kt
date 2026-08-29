package org.rocs.osda.mobile.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.rocs.osda.mobile.data.model.Guardian
import org.rocs.osda.mobile.ui.common.InitialsBadge
import org.rocs.osda.mobile.ui.common.OsdaCard
import org.rocs.osda.mobile.ui.common.StatCard
import org.rocs.osda.mobile.ui.common.StatRow
import org.rocs.osda.mobile.ui.theme.OsdaTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
    val state by viewModel.uiState.collectAsState()
    val darkMode by viewModel.darkMode.collectAsState()
    val person = state.enrollment?.student?.person
    val name = person?.fullName ?: state.studentId ?: "Student"
    val initials = name.split(" ").filter { it.isNotBlank() }.take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
    val program = listOfNotNull(state.enrollment?.student?.studentType, state.enrollment?.section)
        .joinToString(" ")
        .ifBlank { "—" }
    val guardianContact = state.guardians.firstOrNull()?.contactNumber ?: "Not on file"

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = viewModel::load,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            ) {
                InitialsBadge(initials.ifBlank { "?" }, modifier = Modifier.size(52.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(name.uppercase(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Student ID: ${state.studentId ?: "—"}  •  $program",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
            ) {
                StatCard(state.violationsCount.toString(), "Offenses", MaterialTheme.colorScheme.onBackground, Modifier.weight(1f))
                StatCard(state.pendingAppealsCount.toString(), "Pending Appeals", OsdaTokens.amber, Modifier.weight(1f))
                StatCard(state.enrollment?.studentLevel ?: "—", "Year/Level", MaterialTheme.colorScheme.onBackground, Modifier.weight(1f))
            }

            Text("Appearance", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
            OsdaCard(modifier = Modifier.padding(bottom = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // weight(1f) so this column is constrained to the space
                    // left after the Switch, instead of taking its own
                    // unconstrained width -- without it the subtitle ran
                    // full-width and got clipped/overlapped behind the Switch.
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text("Dark Mode", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "App only, not your device.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Switch(
                        checked = darkMode,
                        onCheckedChange = viewModel::setDarkMode,
                        colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }

            Text("Personal Information", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
            OsdaCard(modifier = Modifier.padding(bottom = 20.dp)) {
                StatRow("Name", name)
                Spacer(Modifier.height(8.dp))
                StatRow("Date of Birth", person?.dateOfBirth ?: "Not on file")
                Spacer(Modifier.height(8.dp))
                StatRow("Contact Number", state.enrollment?.student?.contactNumber ?: "Not on file")
                Spacer(Modifier.height(8.dp))
                StatRow("Guardian's Contact", guardianContact)
                Spacer(Modifier.height(8.dp))
                StatRow("Section", state.enrollment?.section ?: "—")
            }

            Text("Guardians", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
            if (state.guardians.isEmpty()) {
                Text(
                    "No guardian information on file.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 20.dp)) {
                    state.guardians.forEach { guardian -> GuardianCard(guardian) }
                }
            }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 20.dp))
            }
        }
    }
}

@Composable
private fun GuardianCard(guardian: Guardian) {
    val name = guardian.person?.fullName ?: "Not on file"
    OsdaCard {
        Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        Text(
            guardian.relationship ?: "Guardian",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
        )
        StatRow("Contact Number", guardian.contactNumber ?: "Not on file")
    }
}