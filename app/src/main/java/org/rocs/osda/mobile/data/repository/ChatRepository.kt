package org.rocs.osda.mobile.data.repository

import org.rocs.osda.mobile.data.model.ChatMessage
import org.rocs.osda.mobile.data.model.ChatRequest
import org.rocs.osda.mobile.data.model.ChatResponse
import org.rocs.osda.mobile.data.remote.ChatApi

/**
 * No SessionManager needed here -- unlike the other repositories, the backend
 * resolves which student is asking purely from the JWT (added automatically by
 * ApiClient's AuthInterceptor), so there's no studentId to pass client-side.
 */
class ChatRepository(private val chatApi: ChatApi) {

    suspend fun ask(message: String, history: List<ChatMessage>): ChatResponse =
        chatApi.sendMessage(ChatRequest(message = message, history = history))
}
