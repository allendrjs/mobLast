package org.rocs.osda.mobile.data.remote

import org.rocs.osda.mobile.data.model.Appeal
import org.rocs.osda.mobile.data.model.AppealSubmission
import org.rocs.osda.mobile.data.model.AppealUpdate
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AppealApi {
    @POST("api/appeals")
    suspend fun submitAppeal(@Body submission: AppealSubmission): Appeal

    @PUT("api/appeals/{id}")
    suspend fun updateAppeal(@Path("id") id: Long, @Body update: AppealUpdate): Appeal

    @GET("api/appeals/student/{studentId}")
    suspend fun getAppealsForStudent(@Path("studentId") studentId: String): List<Appeal>
}