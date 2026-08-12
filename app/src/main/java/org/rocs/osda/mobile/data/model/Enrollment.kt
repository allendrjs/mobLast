package org.rocs.osda.mobile.data.model

data class Enrollment(
    val enrollmentId: Long,
    val student: StudentRef?,
    val schoolYear: String?,
    val studentLevel: String?,
    val section: String?,
    val department: String?,
    val disciplinaryStatus: DisciplinaryStatusRef?
)

data class StudentRef(
    val studentId: String,
    val person: PersonRef?,
    val address: String?,
    val studentType: String?,
    val department: String?,
    val contactNumber: String?
)

data class PersonRef(
    val personID: Long,
    val lastName: String,
    val firstName: String,
    val middleName: String?,
    val dateOfBirth: String?
) {
    val fullName: String
        get() = listOfNotNull(firstName, middleName, lastName).joinToString(" ")
}

data class DisciplinaryStatusRef(
    val disciplinaryStatusId: Long,
    val status: String?,
    val description: String?
)