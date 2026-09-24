package org.rocs.osda.mobile.data.remote

import com.google.gson.Gson
import retrofit2.HttpException

private data class ApiErrorBody(val message: String?)

private val gson = Gson()

fun Throwable.toUserMessage(fallback: String): String {
    if (this is HttpException) {
        val raw = response()?.errorBody()?.string()
        val parsedMessage = raw?.let {
            runCatching { gson.fromJson(it, ApiErrorBody::class.java) }.getOrNull()?.message
        }
        if (!parsedMessage.isNullOrBlank()) {
            return parsedMessage
        }
    }
    return fallback
}
