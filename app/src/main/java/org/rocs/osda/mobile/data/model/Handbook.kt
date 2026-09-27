package org.rocs.osda.mobile.data.model

data class HandbookSection(
    val sectionTitle: String?,
    val content: String
)

data class HandbookResponse(
    val department: String,
    val sections: List<HandbookSection>
)
