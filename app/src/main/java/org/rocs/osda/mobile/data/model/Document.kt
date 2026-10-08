package org.rocs.osda.mobile.data.model

data class DocumentUploadResponse(
    val documentId: Long,
    val extractedText: String?,
    val aiSuggestion: String?
)
private const val LOW_CONFIDENCE_TEXT_LENGTH = 25

fun DocumentUploadResponse.looksUnreadable(): Boolean =
    extractedText != null && (extractedText.isBlank() || extractedText.trim().length < LOW_CONFIDENCE_TEXT_LENGTH)