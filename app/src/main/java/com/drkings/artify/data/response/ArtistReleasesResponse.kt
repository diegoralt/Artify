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
    val artist: String?,
    val year: Int?,
    val thumb: String?,
    val format: String?,
    val label: String?,
    val status: String?
)