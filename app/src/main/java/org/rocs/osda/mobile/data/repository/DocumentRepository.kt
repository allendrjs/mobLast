package org.rocs.osda.mobile.data.repository

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.rocs.osda.mobile.data.model.DocumentUploadResponse
import org.rocs.osda.mobile.data.remote.DocumentApi
import java.io.File

class DocumentRepository(private val documentApi: DocumentApi) {

    suspend fun uploadAppealLetter(file: File, fileName: String, contentType: String): DocumentUploadResponse {
        val body = file.asRequestBody(contentType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", fileName, body)
        return documentApi.uploadAppealDocument(part)
    }

    suspend fun uploadAppealLetter(bytes: ByteArray, fileName: String, contentType: String): DocumentUploadResponse {
        val body = bytes.toRequestBody(contentType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", fileName, body)
        return documentApi.uploadAppealDocument(part)
    }
}
