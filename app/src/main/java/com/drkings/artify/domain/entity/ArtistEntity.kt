package com.drkings.artify.domain.entity

data class ArtistEntity(
    val id: String = "",
    val name: String = "Coldplay",
    val type: ArtistType = ArtistType.BAND,
    val thumbUrl: String? = null,
)

enum class ArtistType(type: String) {
    BAND("Band"),
    ARTIST("Artist")
}


