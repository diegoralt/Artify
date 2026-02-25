package com.drkings.artify.data.response

import kotlinx.serialization.Serializable

@Serializable
data class ArtistResponse(
    val id: Int,
    val type: String,
    val title: String,
    val thumb: String
)