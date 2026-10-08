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
import org.rocs.osda.mobile.data.model.looksUnreadable
import org.rocs.osda.mobile.data.repository.DocumentRepository
import java.io.File

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
    val submitSuccess: Boolean = false,
    val attachmentFileName: String? = null,
    val isUploadingAttachment: Boolean = false,
    val attachmentDocumentId: Long? = null,
    val attachmentLooksUnreadable: Boolean = false,
    val attachmentError: String? = null,
    val editingAppeal: Appeal? = null,
    val editMessage: String = "",
    val isSavingEdit: Boolean = false,
    val editError: String? = null
) {
    val filteredAppeals: List<Appeal>
        get() = when (filter) {
            AppealFilter.ALL -> appeals
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
    private val documentRepository: DocumentRepository,
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

    fun refresh() {
        viewModelScope.launch {
            try {
                val appeals = appealRepository.getMyAppeals().sortedByDescending { it.dateFiled ?: "" }
                _uiState.value = _uiState.value.copy(appeals = appeals)
            } catch (e: Exception) {
            }
        }
    }

    fun startEdit(appeal: Appeal) {
        _uiState.value = _uiState.value.copy(
            editingAppeal = appeal,
            editMessage = appeal.message,
            isSavingEdit = false,
            editError = null
        )
    }

    fun onEditMessageChange(value: String) {
        _uiState.value = _uiState.value.copy(editMessage = value, editError = null)
    }

    fun cancelEdit() {
        _uiState.value = _uiState.value.copy(editingAppeal = null, editError = null)
    }

    fun saveEdit() {
        val state = _uiState.value
        val appeal = state.editingAppeal ?: return
        val newMessage = state.editMessage.trim()
        if (newMessage.isEmpty()) {
            _uiState.value = state.copy(editError = "Appeal message is required.")
            return
        }
        if (newMessage == appeal.message.trim()) {
            cancelEdit()
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSavingEdit = true, editError = null)
            try {
                appealRepository.updateAppeal(appeal.appealId, newMessage)
                val refreshed = appealRepository.getMyAppeals().sortedByDescending { it.dateFiled ?: "" }
                _uiState.value = _uiState.value.copy(
                    isSavingEdit = false,
                    editingAppeal = null,
                    appeals = refreshed
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSavingEdit = false,
                    editError = e.toUserMessage("Couldn't save your changes. Please try again.")
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

    fun uploadAttachmentFromFile(file: File, fileName: String, contentType: String) {
        uploadAttachment { documentRepository.uploadAppealLetter(file, fileName, contentType) }
    }

    fun uploadAttachmentFromBytes(bytes: ByteArray, fileName: String, contentType: String) {
        uploadAttachment { documentRepository.uploadAppealLetter(bytes, fileName, contentType) }
    }

    private fun uploadAttachment(upload: suspend () -> org.rocs.osda.mobile.data.model.DocumentUploadResponse) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUploadingAttachment = true,
                attachmentError = null,
                submitError = null
            )
            try {
                val response = upload()
                _uiState.value = _uiState.value.copy(
                    isUploadingAttachment = false,
                    attachmentDocumentId = response.documentId,
                    attachmentLooksUnreadable = response.looksUnreadable()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUploadingAttachment = false,
                    attachmentDocumentId = null,
                    attachmentError = e.toUserMessage("Couldn't upload that file. Please try again.")
                )
            }
        }
    }

    fun onAttachmentSelected(fileName: String) {
        _uiState.value = _uiState.value.copy(
            attachmentFileName = fileName,
            attachmentDocumentId = null,
            attachmentLooksUnreadable = false,
            attachmentError = null
        )
    }

    fun onAttachmentReadError() {
        _uiState.value = _uiState.value.copy(
            isUploadingAttachment = false,
            attachmentDocumentId = null,
            attachmentError = "Couldn't read the selected file. Please try again."
        )
    }

    fun clearAttachment() {
        _uiState.value = _uiState.value.copy(
            attachmentFileName = null,
            attachmentDocumentId = null,
            attachmentLooksUnreadable = false,
            attachmentError = null,
            isUploadingAttachment = false
        )
    }

    fun validateBeforeConfirm(): Boolean {
        val state = _uiState.value
        val recordId = state.selectedRecordId
        if (recordId == null) {
            _uiState.value = state.copy(submitError = "Please select which offense you're appealing.")
            return false
        }
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
                appealRepository.submitAppeal(
                    recordId, currentEnrollmentId, state.message.trim(), state.attachmentDocumentId
                )
                val refreshed = appealRepository.getMyAppeals().sortedByDescending { it.dateFiled ?: "" }
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    submitSuccess = true,
                    appeals = refreshed,
                    message = "",
                    attachmentFileName = null,
                    attachmentDocumentId = null,
                    attachmentLooksUnreadable = false
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
