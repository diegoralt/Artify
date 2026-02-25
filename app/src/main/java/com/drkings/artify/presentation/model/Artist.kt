package com.drkings.artify.presentation.model

data class Artist(
    val id: String = "",
    val name: String = "Coldplay",
    val type: ArtistType = ArtistType.BAND,
    val thumbUrl: String? = null,
)

enum class ArtistType(type: String) {
    BAND("Band"),
    ARTIST("Artist")
}


