package com.drkings.artify.domain.entity

data class AlbumEntity(
    val id: Int,
    val title: String,
    val artist: String,
    val year: Int?,
    val thumbUrl: String,
    val format: String,
    val label: String,
    val genres: List<String>
)
