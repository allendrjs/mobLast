package org.rocs.osda.mobile.ui.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.rocs.osda.mobile.data.model.OffenseRecord
import org.rocs.osda.mobile.data.repository.RecordRepository

enum class OffenseFilter { ALL, ACTIVE, RESOLVED }

data class RecordsUiState(
    val isLoading: Boolean = false,
    val records: List<OffenseRecord> = emptyList(),
    val filter: OffenseFilter = OffenseFilter.ALL,
    val selectedRecord: OffenseRecord? = null,
    val error: String? = null
) {
    val filteredRecords: List<OffenseRecord>
        get() = when (filter) {
            OffenseFilter.ALL -> records
            OffenseFilter.ACTIVE -> records.filter { it.status.uppercase() != "RESOLVED" }
            OffenseFilter.RESOLVED -> records.filter { it.status.uppercase() == "RESOLVED" }
        }

    val totalCount: Int get() = records.size
    val activeCount: Int get() = records.count { it.status.uppercase() != "RESOLVED" }
    val resolvedCount: Int get() = records.count { it.status.uppercase() == "RESOLVED" }
}

class RecordsViewModel(private val recordRepository: RecordRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordsUiState())
    val uiState: StateFlow<RecordsUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val records = recordRepository.getMyRecords()
                _uiState.value = _uiState.value.copy(isLoading = false, records = records)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Couldn't load your offenses. Please try again."
                )
            }
        }
    }

    fun setFilter(filter: OffenseFilter) {
        _uiState.value = _uiState.value.copy(filter = filter)
    }

    fun selectRecord(record: OffenseRecord) {
        _uiState.value = _uiState.value.copy(selectedRecord = record)
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedRecord = null)
    }
}