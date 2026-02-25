package com.drkings.artify.domain.entity

data class SearchEntity(
    val pagination: PaginationEntity,
    val artists: List<ArtistEntity>
)
