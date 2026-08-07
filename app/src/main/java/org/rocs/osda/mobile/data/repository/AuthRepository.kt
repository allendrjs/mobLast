package org.rocs.osda.mobile.data.repository

import org.rocs.osda.mobile.data.model.LoginRequest
import org.rocs.osda.mobile.data.remote.AuthApi
import org.rocs.osda.mobile.session.SessionManager


class AuthRepository(
    private val authApi: AuthApi,
    private val sessionManager: SessionManager
) {
    suspend fun login(username: String, password: String) {
        val response = authApi.login(LoginRequest(username, password))
        sessionManager.save(response.token, response.username, response.role)
    }

    suspend fun logout() {
        sessionManager.clear()
    }
}
