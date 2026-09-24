package org.rocs.osda.mobile.ui.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.rocs.osda.mobile.data.model.Appeal
import org.rocs.osda.mobile.data.model.OffenseRecord
import org.rocs.osda.mobile.data.remote.toUserMessage
import org.rocs.osda.mobile.data.repository.AppealRepository
import org.rocs.osda.mobile.data.repository.RecordRepository

enum class OffenseFilter { ALL, ACTIVE, RESOLVED }

data class RecordsUiState(
    val isLoading: Boolean = false,
    val records: List<OffenseRecord> = emptyList(),
    val appeals: List<Appeal> = emptyList(),
    val filter: OffenseFilter = OffenseFilter.ALL,
    val error: String? = null
) {
    val filteredRecords: List<OffenseRecord>
        get() = when (filter) {
            OffenseFilter.ALL -> records
            OffenseFilter.ACTIVE -> records.filterNot { it.isClosed() }
            OffenseFilter.RESOLVED -> records.filter { it.isClosed() }
        }

    val totalCount: Int get() = records.size
    val activeCount: Int get() = records.count { !it.isClosed() }
    val resolvedCount: Int get() = records.count { it.isClosed() }

    fun hasActiveAppeal(recordId: Long): Boolean =
        appeals.any { it.record?.recordId == recordId }
}

private fun OffenseRecord.isClosed(): Boolean =
    status.uppercase() in setOf("RESOLVED", "APPROVED")

class RecordsViewModel(
    private val recordRepository: RecordRepository,
    private val appealRepository: AppealRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordsUiState())
    val uiState: StateFlow<RecordsUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val records = recordRepository.getMyRecords().sortedByDescending { it.dateOfViolation }
                val appeals = runCatching { appealRepository.getMyAppeals() }.getOrDefault(emptyList())
                _uiState.value = _uiState.value.copy(isLoading = false, records = records, appeals = appeals)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.toUserMessage("Couldn't load your offenses. Please try again.")
                )
            }
        }
    }

    fun setFilter(filter: OffenseFilter) {
        _uiState.value = _uiState.value.copy(filter = filter)
    }
}