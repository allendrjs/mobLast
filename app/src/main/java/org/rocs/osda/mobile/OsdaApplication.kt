package org.rocs.osda.mobile

import android.app.Application
import org.rocs.osda.mobile.data.remote.ApiClient
import org.rocs.osda.mobile.data.remote.AppealApi
import org.rocs.osda.mobile.data.remote.AuthApi
import org.rocs.osda.mobile.data.remote.ChatApi
import org.rocs.osda.mobile.data.remote.EnrollmentApi
import org.rocs.osda.mobile.data.remote.GuardianApi
import org.rocs.osda.mobile.data.remote.RecordApi
import org.rocs.osda.mobile.data.repository.AppealRepository
import org.rocs.osda.mobile.data.repository.AuthRepository
import org.rocs.osda.mobile.data.repository.ChatRepository
import org.rocs.osda.mobile.data.repository.EnrollmentRepository
import org.rocs.osda.mobile.data.repository.GuardianRepository
import org.rocs.osda.mobile.data.repository.RecordRepository
import org.rocs.osda.mobile.session.SessionManager

class OsdaApplication : Application() {
    lateinit var sessionManager: SessionManager
        private set
    lateinit var authRepository: AuthRepository
        private set
    lateinit var recordRepository: RecordRepository
        private set
    lateinit var appealRepository: AppealRepository
        private set
    lateinit var enrollmentRepository: EnrollmentRepository
        private set
    lateinit var guardianRepository: GuardianRepository
        private set
    lateinit var chatRepository: ChatRepository
        private set

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(this)
        val retrofit = ApiClient.create(sessionManager)

        authRepository = AuthRepository(retrofit.create(AuthApi::class.java), sessionManager)
        recordRepository = RecordRepository(retrofit.create(RecordApi::class.java), sessionManager)
        appealRepository = AppealRepository(retrofit.create(AppealApi::class.java), sessionManager)
        enrollmentRepository = EnrollmentRepository(retrofit.create(EnrollmentApi::class.java), sessionManager)
        guardianRepository = GuardianRepository(retrofit.create(GuardianApi::class.java), sessionManager)
        chatRepository = ChatRepository(retrofit.create(ChatApi::class.java))
    }
}