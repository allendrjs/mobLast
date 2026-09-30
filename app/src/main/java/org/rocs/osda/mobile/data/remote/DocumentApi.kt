package org.rocs.osda.mobile.data.remote

import okhttp3.MultipartBody
import org.rocs.osda.mobile.data.model.DocumentUploadResponse
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface DocumentApi {
    @Multipart
    @POST("api/documents/upload")
    suspend fun uploadAppealDocument(@Part file: MultipartBody.Part): DocumentUploadResponse
}
