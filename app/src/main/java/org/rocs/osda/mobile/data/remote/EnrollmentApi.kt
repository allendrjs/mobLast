package org.rocs.osda.mobile.data.remote

import org.rocs.osda.mobile.data.model.Enrollment
import retrofit2.http.GET
import retrofit2.http.Path

interface EnrollmentApi {
    @GET("api/enrollments/student/{studentId}/latest")
    suspend fun getLatestEnrollment(@Path("studentId") studentId: String): Enrollment
}