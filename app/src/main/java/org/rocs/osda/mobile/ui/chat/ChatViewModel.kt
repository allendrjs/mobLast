package org.rocs.osda.mobile.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.rocs.osda.mobile.data.model.ChatMessage
import org.rocs.osda.mobile.data.model.OffenseRecord
import org.rocs.osda.mobile.data.model.isPending
import org.rocs.osda.mobile.data.remote.toUserMessage
import org.rocs.osda.mobile.data.repository.AppealRepository
import org.rocs.osda.mobile.data.repository.ChatRepository
import org.rocs.osda.mobile.data.repository.EnrollmentRepository
import org.rocs.osda.mobile.data.repository.RecordRepository

/** A tappable option shown below the chat transcript, e.g. "File an Appeal" or a specific offense. */
data class QuickReply(val id: String, val label: String)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val isSending: Boolean = false,
    val error: String? = null,
    val quickReplies: List<QuickReply> = emptyList()
)

/**
 * Tracks the student's place inside the guided "File an Appeal" flow. This
 * is deliberately NOT driven by the LLM -- Ollama only ever answers
 * open-ended handbook questions. Once the flow starts, every step (which
 * offense, what message, whether to submit) is handled locally by this
 * ViewModel and the actual submission goes through the same
 * AppealRepository.submitAppeal() the Appeals tab uses, so a guided-in-chat
 * appeal is exactly as reliable as one filed the normal way.
 */
private sealed class AppealFlowStep {
    data class PickingOffense(val eligible: List<OffenseRecord>) : AppealFlowStep()
    data class WritingMessage(val record: OffenseRecord) : AppealFlowStep()
    data class Confirming(val record: OffenseRecord, val message: String) : AppealFlowStep()
}

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val recordRepository: RecordRepository,
    private val appealRepository: AppealRepository,
    private val enrollmentRepository: EnrollmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState(quickReplies = starterQuickReplies()))
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var flowStep: AppealFlowStep? = null
    private var enrollmentId: Long? = null

    fun onInputChange(value: String) {
        _uiState.value = _uiState.value.copy(input = value, error = null)
    }

    /** Handles a typed message, routing it to the active guided-flow step if there is one. */
    fun send() {
        val state = _uiState.value
        val message = state.input.trim()
        if (message.isBlank() || state.isSending) return

        val historyForRequest = state.messages
        _uiState.value = state.copy(
            messages = state.messages + ChatMessage("user", message),
            input = "",
            quickReplies = emptyList(),
            error = null
        )

        when (val step = flowStep) {
            is AppealFlowStep.PickingOffense -> {
                val matched = step.eligible.firstOrNull { it.offense.offense.contains(message, ignoreCase = true) }
                if (matched != null) {
                    handleOffensePicked(matched)
                } else {
                    appendBotMessage("I didn't recognize that offense. Please pick one below, or type its exact name.")
                    setQuickReplies(offenseQuickReplies(step.eligible))
                }
            }

            is AppealFlowStep.WritingMessage -> handleAppealMessageWritten(message, step.record)

            is AppealFlowStep.Confirming -> {
                when (message.trim().lowercase()) {
                    "yes", "y", "submit", "confirm", "yeah", "yep" -> confirmAppealSubmission()
                    "no", "n", "cancel", "nope" -> cancelAppealFlow()
                    else -> {
                        appendBotMessage("Tap Submit to file the appeal, or Cancel to stop.")
                        setQuickReplies(listOf(QuickReply("confirm_submit", "Submit"), QuickReply("confirm_cancel", "Cancel")))
                    }
                }
            }

            null -> {
                if (looksLikeAppealIntent(message)) {
                    offerAppealChoice()
                } else {
                    dispatchToBot(message, historyForRequest)
                }
            }
        }
    }

    /** Handles a tap on any chip below the transcript -- starter actions, an offense, or a flow confirm/cancel. */
    fun onQuickReplySelected(reply: QuickReply) {
        if (_uiState.value.isSending) return
        appendUserMessage(reply.label)
        setQuickReplies(emptyList())

        when {
            reply.id == "start_appeal" -> offerAppealChoice()
            reply.id == "appeal_in_chat" -> startAppealFlow()
            reply.id == "check_status" -> checkStatus()
            reply.id == "flow_cancel" || reply.id == "confirm_cancel" -> cancelAppealFlow()
            reply.id == "confirm_submit" -> confirmAppealSubmission()
            reply.id.startsWith("offense_") -> {
                val recordId = reply.id.removePrefix("offense_").toLongOrNull()
                val record = (flowStep as? AppealFlowStep.PickingOffense)?.eligible?.firstOrNull { it.recordId == recordId }
                if (record != null) handleOffensePicked(record)
            }
            reply.id.startsWith("topic_") -> {
                // The bubble shows the short label (e.g. "Dress Code"), but the
                // actual question sent to Ollama is the full phrasing below --
                // the backend never sees the label, only this message + history.
                val history = _uiState.value.messages.dropLast(1)
                dispatchToBot(topicQuestionFor(reply.id), history)
            }
        }
    }

    /**
     * Offers the choice this ViewModel now supports before actually starting
     * anything: guided step-by-step in chat, or leave chat and file it the
     * normal way (pick the offense on the Offenses tab, same as before this
     * feature existed).
     */
    private fun offerAppealChoice() {
        appendBotMessage("Would you like to file it right here in chat, or go do it yourself in the app?")
        setQuickReplies(listOf(
            QuickReply("appeal_in_chat", "File Here in Chat"),
            QuickReply("appeal_go_manual", "Go to Offenses")
        ))
    }

    private fun startAppealFlow() {
        flowStep = null
        _uiState.value = _uiState.value.copy(isSending = true)
        viewModelScope.launch {
            try {
                val records = recordRepository.getMyRecords()
                val appeals = appealRepository.getMyAppeals()
                val alreadyAppealed = appeals.mapNotNull { it.record?.recordId }.toSet()
                // Same eligibility rule as RecordsUiState.hasActiveAppeal / AppealViewModel:
                // must be PENDING and not already have an appeal on file, regardless of
                // that appeal's status.
                val eligible = records.filter {
                    it.status.equals("PENDING", ignoreCase = true) && it.recordId !in alreadyAppealed
                }
                if (enrollmentId == null) {
                    enrollmentId = runCatching { enrollmentRepository.getMyLatestEnrollment()?.enrollmentId }.getOrNull()
                }
                _uiState.value = _uiState.value.copy(isSending = false)
                if (eligible.isEmpty()) {
                    appendBotMessage("You don't currently have any offenses that are eligible for an appeal.")
                    setQuickReplies(starterQuickReplies())
                } else {
                    flowStep = AppealFlowStep.PickingOffense(eligible)
                    appendBotMessage("Which offense would you like to appeal?")
                    setQuickReplies(offenseQuickReplies(eligible))
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSending = false)
                appendBotMessage(e.toUserMessage("Couldn't load your offenses right now. Please try again."))
                setQuickReplies(starterQuickReplies())
            }
        }
    }

    /** Read-only lookup, not an LLM call -- counts come straight from the same repositories the Offenses/Appeals tabs use. */
    private fun checkStatus() {
        _uiState.value = _uiState.value.copy(isSending = true)
        viewModelScope.launch {
            try {
                val records = recordRepository.getMyRecords()
                val appeals = appealRepository.getMyAppeals()
                val activeOffenses = records.count { it.status.equals("PENDING", ignoreCase = true) }
                val pendingAppeals = appeals.count { it.isPending() }
                appendBotMessage(
                    "You have ${records.size} offense(s) on file (${activeOffenses} still active), " +
                        "and ${appeals.size} appeal(s) filed (${pendingAppeals} awaiting a decision)."
                )
                _uiState.value = _uiState.value.copy(isSending = false)
                setQuickReplies(
                    listOf(QuickReply("view_offenses", "View My Offenses"), QuickReply("view_appeals", "View My Appeals")) +
                        starterQuickReplies()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSending = false)
                appendBotMessage(e.toUserMessage("Couldn't check your status right now. Please try again."))
                setQuickReplies(starterQuickReplies())
            }
        }
    }

    private fun handleOffensePicked(record: OffenseRecord) {
        flowStep = AppealFlowStep.WritingMessage(record)
        appendBotMessage("Please describe why you're appealing the \"${record.offense.offense}\" record, in your own words.")
        setQuickReplies(listOf(QuickReply("flow_cancel", "Cancel")))
    }

    private fun handleAppealMessageWritten(message: String, record: OffenseRecord) {
        flowStep = AppealFlowStep.Confirming(record, message)
        appendBotMessage("Submit this appeal for \"${record.offense.offense}\"? This can't be edited afterward.")
        setQuickReplies(listOf(QuickReply("confirm_submit", "Submit"), QuickReply("confirm_cancel", "Cancel")))
    }

    private fun confirmAppealSubmission() {
        val step = flowStep as? AppealFlowStep.Confirming ?: return
        flowStep = null
        _uiState.value = _uiState.value.copy(isSending = true)
        viewModelScope.launch {
            try {
                val currentEnrollmentId = enrollmentId
                    ?: enrollmentRepository.getMyLatestEnrollment()?.enrollmentId
                    ?: throw IllegalStateException("No current enrollment on file.")
                enrollmentId = currentEnrollmentId
                appealRepository.submitAppeal(step.record.recordId, currentEnrollmentId, step.message)
                appendBotMessage("Your appeal for \"${step.record.offense.offense}\" has been submitted. Tap below to view it, or keep asking me anything else.")
                _uiState.value = _uiState.value.copy(isSending = false)
                setQuickReplies(listOf(QuickReply("view_appeals", "View My Appeals")) + starterQuickReplies())
            } catch (e: Exception) {
                appendBotMessage(e.toUserMessage("Couldn't submit your appeal. Please try again, or use the Appeals tab."))
                _uiState.value = _uiState.value.copy(isSending = false)
                setQuickReplies(starterQuickReplies())
            }
        }
    }

    private fun cancelAppealFlow() {
        flowStep = null
        appendBotMessage("No problem, the appeal wasn't submitted. Ask me anything else, or start over anytime.")
        setQuickReplies(starterQuickReplies())
    }

    private fun dispatchToBot(message: String, history: List<ChatMessage>) {
        _uiState.value = _uiState.value.copy(isSending = true)
        viewModelScope.launch {
            try {
                val response = chatRepository.ask(message, history)
                appendBotMessage(response.reply)
                // Re-offer the starter chips after a normal answer so the
                // student can keep tapping instead of typing every turn.
                _uiState.value = _uiState.value.copy(isSending = false, quickReplies = starterQuickReplies())
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSending = false,
                    error = e.toUserMessage("Couldn't reach the chatbot. Please try again.")
                )
            }
        }
    }

    private fun appendUserMessage(text: String) {
        _uiState.value = _uiState.value.copy(messages = _uiState.value.messages + ChatMessage("user", text))
    }

    private fun appendBotMessage(text: String) {
        _uiState.value = _uiState.value.copy(messages = _uiState.value.messages + ChatMessage("assistant", text))
    }

    private fun setQuickReplies(replies: List<QuickReply>) {
        _uiState.value = _uiState.value.copy(quickReplies = replies)
    }

    private fun offenseQuickReplies(eligible: List<OffenseRecord>): List<QuickReply> =
        eligible.map { QuickReply("offense_${it.recordId}", it.offense.offense) } + QuickReply("flow_cancel", "Cancel")

    private fun topicQuestionFor(id: String): String = when (id) {
        "topic_dress_code" -> "What is the dress code policy?"
        "topic_attendance" -> "What is the attendance policy?"
        "topic_major_offenses" -> "What counts as a major offense?"
        "topic_bullying" -> "What is the policy on bullying?"
        else -> id.removePrefix("topic_").replace('_', ' ')
    }

    private val appealIntentPhrases = listOf(
        "file an appeal", "file appeal", "want to appeal", "want to file an appeal",
        "start an appeal", "submit an appeal", "make an appeal"
    )

    private fun looksLikeAppealIntent(message: String): Boolean {
        val normalized = message.lowercase()
        return appealIntentPhrases.any { normalized.contains(it) }
    }

    companion object {
        fun starterQuickReplies(): List<QuickReply> = listOf(
            QuickReply("start_appeal", "File an Appeal"),
            QuickReply("check_status", "Check My Offenses/Appeals"),
            QuickReply("topic_dress_code", "Dress Code"),
            QuickReply("topic_attendance", "Attendance"),
            QuickReply("topic_major_offenses", "Major Offenses"),
            QuickReply("topic_bullying", "Bullying Policy")
        )
    }
}
