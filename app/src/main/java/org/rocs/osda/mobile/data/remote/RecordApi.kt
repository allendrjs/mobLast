package org.rocs.osda.mobile.data.remote

import org.rocs.osda.mobile.data.model.OffenseRecord
import retrofit2.http.GET
import retrofit2.http.Path

interface RecordApi {
    @GET("api/records/student/{studentId}")
    suspend fun getRecordsForStudent(@Path("studentId") studentId: String): List<OffenseRecord>
}