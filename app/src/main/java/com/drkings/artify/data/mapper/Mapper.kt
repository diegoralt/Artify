package com.drkings.artify.data.mapper

import com.drkings.artify.data.response.ArtistDetailResponse
import com.drkings.artify.data.response.ArtistResponse
import com.drkings.artify.data.response.MemberResponse
import com.drkings.artify.data.response.PaginationResponse
import com.drkings.artify.data.response.SearchResponse
import com.drkings.artify.domain.entity.ArtistDetailEntity
import com.drkings.artify.domain.entity.ArtistEntity
import com.drkings.artify.domain.entity.MemberEntity
import com.drkings.artify.domain.entity.PaginationEntity
import com.drkings.artify.domain.entity.SearchEntity

// ── Search mappers ───────────────────────────────────────────────────────────
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

// ── Artist detail mappers ───────────────────────────────────────────────────────────
fun ArtistDetailResponse.toDomain(): ArtistDetailEntity {
    return ArtistDetailEntity(
        id = id,
        name = name,
        profile = profile,
        image = images.find { it.type == "primary" }?.resourceUrl.orEmpty(),
        members = members.map { it.toDomain() }
    )
}

fun MemberResponse.toDomain(): MemberEntity {
    return MemberEntity(
        id = id,
        name = name,
        imageUrl = thumbnailUrl.orEmpty()
    )
}