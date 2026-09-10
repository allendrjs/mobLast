package org.rocs.osda.mobile.data.remote

import com.google.gson.Gson
import retrofit2.HttpException

/**
 * Backend errors (see GlobalExceptionHandler on the server) come back as a
 * JSON body like {"timestamp": ..., "status": 409, "code": "CONFLICT",
 * "message": "This offense has already been appealed and cannot be
 * appealed again."}.
 *
 * Retrofit's HttpException.message is just "HTTP 409 Conflict" -- it never
 * contains that body. Without this, callers that display e.message end up
 * showing the raw status line instead of the real, user-facing reason.
 *
 * For anything that ISN'T a structured API error -- a dropped connection,
 * a timeout, DNS failure, etc. -- we deliberately never show the raw
 * exception message (e.g. "Software caused connection abort", "Unable to
 * resolve host") to the user. Those are implementation details, not
 * something a student can act on; the caller-supplied fallback is always
 * used instead.
 */
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
