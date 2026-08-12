package org.rocs.osda.mobile.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.rocs.osda.mobile.data.model.Appeal
import org.rocs.osda.mobile.data.model.Enrollment
import org.rocs.osda.mobile.data.model.OffenseRecord
import org.rocs.osda.mobile.data.repository.AppealRepository
import org.rocs.osda.mobile.data.repository.EnrollmentRepository
import org.rocs.osda.mobile.data.repository.RecordRepository
import org.rocs.osda.mobile.session.SessionManager

sealed class ActivityItem(val date: String?) {
    data class RecordLogged(val record: OffenseRecord) : ActivityItem(record.dateOfViolation)
    data class AppealUpdated(val appeal: Appeal) : ActivityItem(appeal.dateProcessed ?: appeal.dateFiled)
}

data class DashboardUiState(
    val isLoading: Boolean = false,
    val studentId: String? = null,
    val enrollment: Enrollment? = null,
    val violationsCount: Int = 0,
    val pendingAppealsCount: Int = 0,
    val recentActivity: List<ActivityItem> = emptyList(),
    val error: String? = null
)

class DashboardViewModel(
    private val sessionManager: SessionManager,
    private val enrollmentRepository: EnrollmentRepository,
    private val recordRepository: RecordRepository,
    private val appealRepository: AppealRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val studentId = sessionManager.studentIdFlow.first()
                val enrollment = runCatching { enrollmentRepository.getMyLatestEnrollment() }.getOrNull()
                val records = runCatching { recordRepository.getMyRecords() }.getOrDefault(emptyList())
                val appeals = runCatching { appealRepository.getMyAppeals() }.getOrDefault(emptyList())

                val activity = (records.map { ActivityItem.RecordLogged(it) } +
                        appeals.filter { it.status.uppercase() != "PENDING" }.map { ActivityItem.AppealUpdated(it) })
                    .sortedByDescending { it.date ?: "" }
                    .take(3)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    studentId = studentId,
                    enrollment = enrollment,
                    violationsCount = records.size,
                    pendingAppealsCount = appeals.count { it.status.uppercase() == "PENDING" || it.status.uppercase() == "UNDER_REVIEW" },
                    recentActivity = activity
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Couldn't load your dashboard. Please try again."
                )
            }
        }
    }
}