package org.rocs.osda.mobile.data.repository

import org.rocs.osda.mobile.data.model.Enrollment
import org.rocs.osda.mobile.data.remote.EnrollmentApi
import org.rocs.osda.mobile.session.SessionManager

class EnrollmentRepository(
    private val enrollmentApi: EnrollmentApi,
    private val sessionManager: SessionManager
) {
    suspend fun getMyLatestEnrollment(): Enrollment {
        val studentId = sessionManager.currentStudentId()
            ?: throw IllegalStateException("No signed-in student.")
        return enrollmentApi.getLatestEnrollment(studentId)
    }
}