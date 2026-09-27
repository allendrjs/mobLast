package org.rocs.osda.mobile.data.repository

import org.rocs.osda.mobile.data.model.HandbookResponse
import org.rocs.osda.mobile.data.remote.HandbookApi

class HandbookRepository(private val handbookApi: HandbookApi) {

    suspend fun getHandbook(): HandbookResponse = handbookApi.getHandbook()
}