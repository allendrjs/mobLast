package org.rocs.osda.mobile.data.remote

import org.rocs.osda.mobile.data.model.Guardian
import retrofit2.http.GET
import retrofit2.http.Path

interface GuardianApi {
    @GET("api/guardians/student/{studentId}")
    suspend fun getGuardiansForStudent(@Path("studentId") studentId: String): List<Guardian>
}