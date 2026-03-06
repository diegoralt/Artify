package com.drkings.artify.data.datasource.api.response

import kotlinx.serialization.Serializable

@Serializable
data class ReleaseDetailResponse(
    val id: Int,
    val genres: List<String>?
)
