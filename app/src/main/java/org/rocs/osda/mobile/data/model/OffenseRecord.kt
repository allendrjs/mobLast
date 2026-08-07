package org.rocs.osda.mobile.data.model

data class ViolationRecord(
    val recordId: Long,
    val enrollment: EnrollmentSummary?,
    val employee: EmployeeSummary?,
    val offense: OffenseSummary,
    val dateOfViolation: String,
    val action: ActionSummary?,
    val dateOfResolution: String?,
    val remarks: String?,
    val status: String
)