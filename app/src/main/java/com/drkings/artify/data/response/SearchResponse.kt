package com.drkings.artify.data.response

import kotlinx.serialization.Serializable

@Serializable
data class SearchResponse(
    val pagination: PaginationResponse,
    val results: List<ArtistResponse>
)

