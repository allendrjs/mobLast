package org.rocs.osda.mobile.data.remote

import org.rocs.osda.mobile.data.model.Appeal
import org.rocs.osda.mobile.data.model.AppealSubmission
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AppealApi {
    @POST("api/appeals")
    suspend fun submitAppeal(@Body submission: AppealSubmission): Appeal

    @GET("api/appeals/student/{studentId}")
    suspend fun getAppealsForStudent(@Path("studentId") studentId: String): List<Appeal>
}