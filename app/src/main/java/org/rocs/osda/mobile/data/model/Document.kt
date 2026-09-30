package org.rocs.osda.mobile.data.model

data class DocumentUploadResponse(
    val documentId: Long,
    val extractedText: String?,
    val aiSuggestion: String?
)

/**
 * Anything under this many characters of extracted text is treated as a
 * likely-unreadable scan (blurry photo, blank page, wrong file, etc.) and
 * surfaces a non-blocking warning in the UI -- the student can still
 * submit, but is nudged to retake/reselect first.
 */
private const val LOW_CONFIDENCE_TEXT_LENGTH = 25

fun DocumentUploadResponse.looksUnreadable(): Boolean =
    extractedText.isNullOrBlank() || extractedText.trim().length < LOW_CONFIDENCE_TEXT_LENGTH
