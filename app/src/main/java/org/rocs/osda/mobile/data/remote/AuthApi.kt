package org.rocs.osda.mobile.data.remote

import org.rocs.osda.mobile.data.model.LoginRequest
import org.rocs.osda.mobile.data.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}
