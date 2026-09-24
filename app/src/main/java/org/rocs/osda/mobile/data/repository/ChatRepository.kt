package org.rocs.osda.mobile.data.repository

import org.rocs.osda.mobile.data.model.ChatMessage
import org.rocs.osda.mobile.data.model.ChatRequest
import org.rocs.osda.mobile.data.model.ChatResponse
import org.rocs.osda.mobile.data.remote.ChatApi

class ChatRepository(private val chatApi: ChatApi) {

    suspend fun ask(message: String, history: List<ChatMessage>): ChatResponse =
        chatApi.sendMessage(ChatRequest(message = message, history = history))
}
