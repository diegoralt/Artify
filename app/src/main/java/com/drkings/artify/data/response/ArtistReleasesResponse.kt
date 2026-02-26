package com.drkings.artify.data.response

import kotlinx.serialization.Serializable

@Serializable
class ArtistReleasesResponse(
    val pagination: PaginationResponse,
    val releases: List<ReleaseResponse>

)

@Serializable
data class ReleaseResponse(
    val id: Int,
    val title: String,
    val artist: String? = null,
    val year: Int? = null,
    val thumb: String? = null,
    val format: String? = null,
    val label: String? = null,
    val status: String? = null
)