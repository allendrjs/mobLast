package org.rocs.osda.mobile.data.repository

import org.rocs.osda.mobile.data.model.OffenseRecord
import org.rocs.osda.mobile.data.remote.RecordApi
import org.rocs.osda.mobile.session.SessionManager

class RecordRepository(
    private val recordApi: RecordApi,
    private val sessionManager: SessionManager
) {
    suspend fun getMyRecords(): List<OffenseRecord> {
        val studentId = sessionManager.currentStudentId()
            ?: throw IllegalStateException("No signed-in student.")
        return recordApi.getRecordsForStudent(studentId)
    }
}