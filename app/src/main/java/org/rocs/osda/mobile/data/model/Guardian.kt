package org.rocs.osda.mobile.data.model

data class Guardian(
    val guardianID: Long,
    val person: PersonRef?,
    val contactNumber: String?,
    val relationship: String?
)
