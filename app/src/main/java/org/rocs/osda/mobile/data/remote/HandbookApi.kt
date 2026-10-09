package org.rocs.osda.mobile.data.remote

import org.rocs.osda.mobile.data.model.HandbookResponse
import retrofit2.http.GET

interface HandbookApi {
    @GET("api/handbook")
    suspend fun getHandbook(): HandbookResponse
}
