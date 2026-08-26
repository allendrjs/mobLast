package org.rocs.osda.mobile.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.rocs.osda.mobile.data.model.ChatMessage
import org.rocs.osda.mobile.data.remote.toUserMessage
import org.rocs.osda.mobile.data.repository.ChatRepository

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val isSending: Boolean = false,
    val error: String? = null
)

class ChatViewModel(private val chatRepository: ChatRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun onInputChange(value: String) {
        _uiState.value = _uiState.value.copy(input = value, error = null)
    }

    fun send() {
        val state = _uiState.value
        val message = state.input.trim()
        if (message.isBlank() || state.isSending) return

        val historyForRequest = state.messages
        val updatedMessages = state.messages + ChatMessage("user", message)

        _uiState.value = state.copy(
            messages = updatedMessages,
            input = "",
            isSending = true,
            error = null
        )

        viewModelScope.launch {
            try {
                val response = chatRepository.ask(message, historyForRequest)
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + ChatMessage("assistant", response.reply),
                    isSending = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSending = false,
                    error = e.toUserMessage("Couldn't reach the chatbot. Please try again.")
                )
            }
        }
    }
}
