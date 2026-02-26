package com.drkings.artify.data.mapper

import com.drkings.artify.data.response.ArtistDetailResponse
import com.drkings.artify.data.response.ArtistReleasesResponse
import com.drkings.artify.data.response.ArtistResponse
import com.drkings.artify.data.response.MemberResponse
import com.drkings.artify.data.response.PaginationResponse
import com.drkings.artify.data.response.ReleaseResponse
import com.drkings.artify.data.response.SearchResponse
import com.drkings.artify.domain.entity.AlbumEntity
import com.drkings.artify.domain.entity.AlbumsDetailEntity
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
        image = images.find { it.type == "primary" }?.resourceUrl.orEmpty()
    )
}

fun ArtistDetailResponse.toDomain(imageByArtistId: Map<Int, String>): ArtistDetailEntity {
    return ArtistDetailEntity(
        id = id,
        name = name,
        profile = profile,
        image = images.find { it.type == "primary" }?.resourceUrl.orEmpty(),
        members = members?.map { it.toDomain(imageByArtistId[it.id].orEmpty()) }
    )
}

fun MemberResponse.toDomain(thumbnailUrl: String): MemberEntity {
    return MemberEntity(
        id = id,
        name = name,
        imageUrl = thumbnailUrl
    )
}

// ── Albums mappers ────────────────────────────────────────────────────────────
fun ArtistReleasesResponse.toDomain(genresByReleaseId: Map<Int, List<String>>): AlbumsDetailEntity {
    return AlbumsDetailEntity(
        pagination = pagination.toDomain(),
        albums = releases.map { it.toDomain(genresByReleaseId[it.id].orEmpty()) })
}

fun ReleaseResponse.toDomain(genres: List<String>): AlbumEntity {
    return AlbumEntity(
        id = id,
        title = title,
        artist = artist.orEmpty(),
        year = year,
        thumbUrl = thumb.orEmpty(),
        format = format.orEmpty(),
        label = label.orEmpty(),
        genres = genres
    )
}