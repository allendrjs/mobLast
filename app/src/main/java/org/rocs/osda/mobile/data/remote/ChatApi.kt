package org.rocs.osda.mobile.data.remote

import org.rocs.osda.mobile.data.model.ChatRequest
import org.rocs.osda.mobile.data.model.ChatResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatApi {
    @POST("api/chat")
    suspend fun sendMessage(@Body request: ChatRequest): ChatResponse
}
