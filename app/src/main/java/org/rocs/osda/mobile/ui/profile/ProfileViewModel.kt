package org.rocs.osda.mobile.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.rocs.osda.mobile.data.model.Enrollment
import org.rocs.osda.mobile.data.model.Guardian
import org.rocs.osda.mobile.data.repository.AppealRepository
import org.rocs.osda.mobile.data.repository.EnrollmentRepository
import org.rocs.osda.mobile.data.repository.GuardianRepository
import org.rocs.osda.mobile.data.repository.RecordRepository
import org.rocs.osda.mobile.session.SessionManager

data class ProfileUiState(
    val isLoading: Boolean = false,
    val studentId: String? = null,
    val enrollment: Enrollment? = null,
    val guardians: List<Guardian> = emptyList(),
    val violationsCount: Int = 0,
    val pendingAppealsCount: Int = 0,
    val error: String? = null
)

class ProfileViewModel(
    private val sessionManager: SessionManager,
    private val enrollmentRepository: EnrollmentRepository,
    private val guardianRepository: GuardianRepository,
    private val recordRepository: RecordRepository,
    private val appealRepository: AppealRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val studentId = sessionManager.studentIdFlow.first()
                val enrollment = runCatching { enrollmentRepository.getMyLatestEnrollment() }.getOrNull()
                val guardians = runCatching { guardianRepository.getMyGuardians() }.getOrDefault(emptyList())
                val records = runCatching { recordRepository.getMyRecords() }.getOrDefault(emptyList())
                val appeals = runCatching { appealRepository.getMyAppeals() }.getOrDefault(emptyList())

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    studentId = studentId,
                    enrollment = enrollment,
                    guardians = guardians,
                    violationsCount = records.size,
                    pendingAppealsCount = appeals.count { it.status.uppercase() == "PENDING" || it.status.uppercase() == "UNDER_REVIEW" }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Couldn't load your profile. Please try again."
                )
            }
        }
    }
}