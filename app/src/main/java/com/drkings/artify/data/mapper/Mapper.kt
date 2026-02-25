package com.drkings.artify.data.mapper

import com.drkings.artify.data.response.ArtistResponse
import com.drkings.artify.data.response.PaginationResponse
import com.drkings.artify.data.response.SearchResponse
import com.drkings.artify.domain.entity.ArtistEntity
import com.drkings.artify.domain.entity.PaginationEntity
import com.drkings.artify.domain.entity.SearchEntity

fun SearchResponse.toDomain(): SearchEntity {
    return SearchEntity(
        pagination = pagination.toDomain(),
        artists = results.map { it.toDomain() }
    )
}

fun PaginationResponse.toDomain(): PaginationEntity {
    return PaginationEntity(
        page = page,
        pages = pages,
        items = items
    )
}

fun ArtistResponse.toDomain(): ArtistEntity {
    return ArtistEntity(
        id = id,
        name = title,
        type = type,
        thumbUrl = thumb
    )
}