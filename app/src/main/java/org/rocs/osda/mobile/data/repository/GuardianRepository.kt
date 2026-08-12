package org.rocs.osda.mobile.data.repository

import org.rocs.osda.mobile.data.model.Guardian
import org.rocs.osda.mobile.data.remote.GuardianApi
import org.rocs.osda.mobile.session.SessionManager

class GuardianRepository(
    private val guardianApi: GuardianApi,
    private val sessionManager: SessionManager
) {
    suspend fun getMyGuardians(): List<Guardian> {
        val studentId = sessionManager.currentStudentId()
            ?: throw IllegalStateException("No signed-in student.")
        return guardianApi.getGuardiansForStudent(studentId)
    }
}