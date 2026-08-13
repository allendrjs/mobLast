package org.rocs.osda.mobile.ui.appeal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.rocs.osda.mobile.data.model.Appeal
import org.rocs.osda.mobile.data.model.OffenseRecord
import org.rocs.osda.mobile.data.repository.AppealRepository
import org.rocs.osda.mobile.data.repository.EnrollmentRepository
import org.rocs.osda.mobile.data.repository.RecordRepository

data class AppealUiState(
    val isLoading: Boolean = false,
    val appeals: List<Appeal> = emptyList(),
    val records: List<OffenseRecord> = emptyList(),
    val selectedRecordId: Long? = null,
    val message: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val submitError: String? = null,
    val submitSuccess: Boolean = false
)

class AppealViewModel(
    private val appealRepository: AppealRepository,
    private val recordRepository: RecordRepository,
    private val enrollmentRepository: EnrollmentRepository,
    initialRecordId: Long? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppealUiState(selectedRecordId = initialRecordId))
    val uiState: StateFlow<AppealUiState> = _uiState.asStateFlow()

    private var enrollmentId: Long? = null

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val appeals = appealRepository.getMyAppeals()
                val records = runCatching { recordRepository.getMyRecords() }.getOrDefault(emptyList())
                val enrollment = runCatching { enrollmentRepository.getMyLatestEnrollment() }.getOrNull()
                enrollmentId = enrollment?.enrollmentId
                _uiState.value = _uiState.value.copy(isLoading = false, appeals = appeals, records = records)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Couldn't load your appeals. Please try again."
                )
            }
        }
    }

    fun selectRecord(recordId: Long) {
        _uiState.value = _uiState.value.copy(selectedRecordId = recordId, submitError = null)
    }

    fun onMessageChange(value: String) {
        _uiState.value = _uiState.value.copy(message = value, submitError = null)
    }

    fun submit() {
        val state = _uiState.value
        val recordId = state.selectedRecordId
        if (recordId == null) {
            _uiState.value = state.copy(submitError = "Please select which offense you're appealing.")
            return
        }
        if (state.message.isBlank()) {
            _uiState.value = state.copy(submitError = "Please enter a message explaining your appeal.")
            return
        }
        val currentEnrollmentId = enrollmentId
        if (currentEnrollmentId == null) {
            _uiState.value = state.copy(submitError = "Couldn't determine your current enrollment. Please try again later.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, submitError = null)
            try {
                appealRepository.submitAppeal(recordId, currentEnrollmentId, state.message.trim())
                val refreshed = appealRepository.getMyAppeals()
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    submitSuccess = true,
                    appeals = refreshed,
                    selectedRecordId = null,
                    message = ""
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    submitError = e.message ?: "Couldn't submit your appeal. Please try again."
                )
            }
        }
    }
}