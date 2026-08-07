package org.rocs.osda.mobile.data.model

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val username: String,
    val role: String?,
    val authorities: String?,
    val personId: Long? = null
)

data class ApiErrorBody(
    val timestamp: String,
    val status: Int,
    val code: String,
    val message: String
)
