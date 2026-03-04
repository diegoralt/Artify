package com.drkings.artify.domain.entity

data class ArtistEntity(
    val id: Int,
    val uuid: String,
    val name: String,
    val type: String,
    val thumbUrl: String
)
