package org.rocs.osda.mobile.data.model

data class Appeal(
    val appealId: Long,
    val record: RecordSummary?,
    val enrollment: EnrollmentSummary?,
    val message: String,
    val dateFiled: String?,
    val status: String,
    val dateProcessed: String?,
    val remarks: String?
)

data class AppealSubmission(
    val recordId: Long,
    val enrollmentId: Long,
    val message: String
)