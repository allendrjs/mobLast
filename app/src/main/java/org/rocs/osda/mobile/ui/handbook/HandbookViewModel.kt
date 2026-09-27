package org.rocs.osda.mobile.ui.handbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.rocs.osda.mobile.data.model.HandbookSection
import org.rocs.osda.mobile.data.remote.toUserMessage
import org.rocs.osda.mobile.data.repository.HandbookRepository

data class HandbookUiState(
    val isLoading: Boolean = false,
    val department: String? = null,
    val sections: List<HandbookSection> = emptyList(),
    val query: String = "",
    val error: String? = null
) {
    val filteredSections: List<HandbookSection>
        get() = if (query.isBlank()) {
            sections
        } else {
            sections.filter {
                (it.sectionTitle?.contains(query, ignoreCase = true) == true) ||
                        it.content.contains(query, ignoreCase = true)
            }
        }
}

class HandbookViewModel(
    private val handbookRepository: HandbookRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HandbookUiState())
    val uiState: StateFlow<HandbookUiState> = _uiState.asStateFlow()

    private var loaded = false

    fun loadIfNeeded() {
        if (loaded) return
        load()
    }

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val response = handbookRepository.getHandbook()
                loaded = true
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    department = response.department,
                    sections = response.sections
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.toUserMessage("Couldn't load the Student Handbook right now. Please try again.")
                )
            }
        }
    }

    fun onQueryChange(value: String) {
        _uiState.value = _uiState.value.copy(query = value)
    }
}