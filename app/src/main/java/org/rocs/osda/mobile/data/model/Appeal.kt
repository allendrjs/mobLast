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

/**
 * An appeal counts as "pending" (i.e. still awaiting a decision) if its
 * status is PENDING or UNDER_REVIEW. Shared across Dashboard, Profile, and
 * the Appeals tab so they all agree on the same "pending appeals" count for
 * the same student instead of each screen defining its own subtly
 * different rule.
 */
fun Appeal.isPending(): Boolean = status.uppercase() in setOf("PENDING", "UNDER_REVIEW")