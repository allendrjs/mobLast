package org.rocs.osda.mobile.data.model

data class ChatMessage(
    val role: String,
    val content: String
)

data class ChatRequest(
    val message: String,
    val history: List<ChatMessage>
)

data class ChatResponse(
    val reply: String,
    val timestamp: String?
)
