package org.rocs.osda.mobile.data.model

data class OffenseSummary(
    val offenseId: Long,
    val offense: String,
    val type: String?
)

data class EmployeeSummary(
    val employeeId: String,
    val fullName: String?,
    val employeeRole: String?
)

data class ActionSummary(
    val actionId: Long,
    val actionName: String?
)

data class StudentSummary(
    val studentId: String,
    val fullName: String?
)

data class EnrollmentSummary(
    val enrollmentId: Long,
    val student: StudentSummary?,
    val schoolYear: String?,
    val studentLevel: String?,
    val section: String?,
    val department: String?
)
