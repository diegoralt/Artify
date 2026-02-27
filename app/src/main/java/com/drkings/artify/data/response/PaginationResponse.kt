package com.drkings.artify.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaginationResponse(
    val page: Int,
    val pages: Int,
    @SerialName("per_page")
    val perPage: Int,
    val items: Int
)
