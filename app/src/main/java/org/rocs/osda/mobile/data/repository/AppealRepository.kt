package org.rocs.osda.mobile.data.repository

import org.rocs.osda.mobile.data.model.Appeal
import org.rocs.osda.mobile.data.model.AppealSubmission
import org.rocs.osda.mobile.data.remote.AppealApi
import org.rocs.osda.mobile.session.SessionManager

class AppealRepository(
    private val appealApi: AppealApi,
    private val sessionManager: SessionManager
) {
    suspend fun submitAppeal(recordId: Long, enrollmentId: Long, message: String): Appeal =
        appealApi.submitAppeal(AppealSubmission(recordId, enrollmentId, message))

    suspend fun getMyAppeals(): List<Appeal> {
        val studentId = sessionManager.currentStudentId()
            ?: throw IllegalStateException("No signed-in student.")
        return appealApi.getAppealsForStudent(studentId)
    }
}