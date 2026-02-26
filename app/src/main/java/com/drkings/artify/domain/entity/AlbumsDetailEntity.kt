package com.drkings.artify.domain.entity

data class AlbumsDetailEntity(
    val pagination: PaginationEntity,
    val albums: List<AlbumEntity>
)
