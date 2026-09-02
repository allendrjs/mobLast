package org.rocs.osda.mobile.ui.appeal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.rocs.osda.mobile.data.model.Appeal
import org.rocs.osda.mobile.data.model.OffenseRecord
import org.rocs.osda.mobile.data.model.isPending
import org.rocs.osda.mobile.data.remote.toUserMessage
import org.rocs.osda.mobile.data.repository.AppealRepository
import org.rocs.osda.mobile.data.repository.EnrollmentRepository
import org.rocs.osda.mobile.data.repository.RecordRepository

enum class AppealFilter { ALL, PENDING, APPROVED, DENIED }

data class AppealUiState(
    val isLoading: Boolean = false,
    val appeals: List<Appeal> = emptyList(),
    val records: List<OffenseRecord> = emptyList(),
    val selectedRecordId: Long? = null,
    val filter: AppealFilter = AppealFilter.ALL,
    val message: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val submitError: String? = null,
    val submitSuccess: Boolean = false
) {
    val filteredAppeals: List<Appeal>
        get() = when (filter) {
            AppealFilter.ALL -> appeals
            // Includes UNDER_REVIEW as well as PENDING, matching pendingCount
            // below and the Dashboard/Profile "Pending Appeals" stat -- an
            // appeal awaiting a decision should count as pending everywhere,
            // not just here.
            AppealFilter.PENDING -> appeals.filter { it.isPending() }
            AppealFilter.APPROVED -> appeals.filter { it.status.uppercase() == "APPROVED" }
            AppealFilter.DENIED -> appeals.filter { it.status.uppercase() == "DENIED" }
        }

    val totalCount: Int get() = appeals.size
    val pendingCount: Int get() = appeals.count { it.isPending() }
    val approvedCount: Int get() = appeals.count { it.status.uppercase() == "APPROVED" }
    val deniedCount: Int get() = appeals.count { it.status.uppercase() == "DENIED" }
}

class AppealViewModel(
    private val appealRepository: AppealRepository,
    private val recordRepository: RecordRepository,
    private val enrollmentRepository: EnrollmentRepository,
    initialRecordId: Long? = null
) : ViewModel() {

    val isFilingMode: Boolean = initialRecordId != null

    private val _uiState = MutableStateFlow(AppealUiState(selectedRecordId = initialRecordId))
    val uiState: StateFlow<AppealUiState> = _uiState.asStateFlow()

    private var enrollmentId: Long? = null

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Most recently filed appeal on top; appeals with no filed
                // date (shouldn't normally happen) sort to the bottom.
                val appeals = appealRepository.getMyAppeals().sortedByDescending { it.dateFiled ?: "" }
                val records = runCatching { recordRepository.getMyRecords() }.getOrDefault(emptyList())
                val enrollment = runCatching { enrollmentRepository.getMyLatestEnrollment() }.getOrNull()
                enrollmentId = enrollment?.enrollmentId
                _uiState.value = _uiState.value.copy(isLoading = false, appeals = appeals, records = records)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.toUserMessage("Couldn't load your appeals. Please try again.")
                )
            }
        }
    }

    fun setFilter(filter: AppealFilter) {
        _uiState.value = _uiState.value.copy(filter = filter)
    }

    fun onMessageChange(value: String) {
        _uiState.value = _uiState.value.copy(message = value, submitError = null)
    }

    /**
     * Runs the same checks [submit] does, without actually submitting.
     * Called before showing the "Submit this appeal?" confirmation dialog so
     * a blank message or an already-appealed record is caught up front,
     * instead of the user confirming "this can't be edited" and only then
     * being told the message was required. Returns true (and leaves
     * [AppealUiState.submitError] untouched) if it's safe to show the
     * dialog; returns false and sets submitError otherwise.
     */
    fun validateBeforeConfirm(): Boolean {
        val state = _uiState.value
        val recordId = state.selectedRecordId
        if (recordId == null) {
            _uiState.value = state.copy(submitError = "Please select which offense you're appealing.")
            return false
        }
        // The backend allows exactly one appeal per offense record, regardless of
        // that appeal's status (see AppealServiceImpl.submitAppeal) -- so any
        // existing appeal for this record, approved or not, blocks a resubmission.
        val alreadyHasAppeal = state.appeals.any { it.record?.recordId == recordId }
        if (alreadyHasAppeal) {
            _uiState.value = state.copy(
                submitError = "You already have an appeal on file for this offense."
            )
            return false
        }
        if (state.message.isBlank()) {
            _uiState.value = state.copy(submitError = "Please enter a message explaining your appeal.")
            return false
        }
        if (enrollmentId == null) {
            _uiState.value = state.copy(submitError = "Couldn't determine your current enrollment. Please try again later.")
            return false
        }
        return true
    }

    fun submit() {
        if (!validateBeforeConfirm()) return
        val state = _uiState.value
        val recordId = state.selectedRecordId ?: return
        val currentEnrollmentId = enrollmentId ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, submitError = null)
            try {
                appealRepository.submitAppeal(recordId, currentEnrollmentId, state.message.trim())
                val refreshed = appealRepository.getMyAppeals().sortedByDescending { it.dateFiled ?: "" }
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    submitSuccess = true,
                    appeals = refreshed,
                    message = ""
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    submitError = e.toUserMessage("Couldn't submit your appeal. Please try again.")
                )
            }
        }
    }
}
