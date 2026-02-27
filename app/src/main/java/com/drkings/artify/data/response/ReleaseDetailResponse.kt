package com.drkings.artify.data.response

import kotlinx.serialization.Serializable

@Serializable
data class ReleaseDetailResponse(
    val id: Int,
    val genres: List<String>?
)
