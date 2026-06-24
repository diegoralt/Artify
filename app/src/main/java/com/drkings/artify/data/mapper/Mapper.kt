package com.drkings.artify.data.mapper

import com.drkings.artify.data.datasource.api.response.ArtistReleasesResponse
import com.drkings.artify.data.datasource.api.response.PaginationResponse
import com.drkings.artify.data.datasource.api.response.ReleaseResponse
import com.drkings.artify.data.datasource.storage.entity.AlbumWithGenre
import com.drkings.artify.data.datasource.storage.entity.Artist
import com.drkings.artify.data.datasource.storage.entity.ArtistWithMember
import com.drkings.artify.data.datasource.storage.entity.Member
import com.drkings.artify.data.datasource.storage.entity.Pagination
import com.drkings.artify.domain.entity.AlbumEntity
import com.drkings.artify.domain.entity.AlbumsDetailEntity
import com.drkings.artify.domain.entity.ArtistDetailEntity
import com.drkings.artify.domain.entity.ArtistEntity
import com.drkings.artify.domain.entity.MemberEntity
import com.drkings.artify.domain.entity.PaginationEntity
import com.drkings.artify.domain.entity.SearchEntity
import java.util.UUID

// ── Search mappers ───────────────────────────────────────────────────────────
fun List<Artist>.toDomain(pageCache: Pagination): SearchEntity {
    return SearchEntity(
        pagination = pageCache.toDomain(),
        artists = map { it.toDomain() }
    )
}

fun Pagination.toDomain(): PaginationEntity {
    return PaginationEntity(
        page = page,
        pages = totalPages,
        items = totalItems
    )
}

fun PaginationResponse.toDomain(): PaginationEntity {
    return PaginationEntity(
        page = page,
        pages = pages,
        items = items
    )
}

fun Artist.toDomain(): ArtistEntity {
    return ArtistEntity(
        id = id,
        uuid = uuid,
        name = name,
        type = type,
        thumbUrl = thumb
    )
}

// ── Artist detail mappers ─────────────────────────────────────────────────────────────
fun ArtistWithMember.toDomain(): ArtistDetailEntity {
    return ArtistDetailEntity(
        id = artist.id,
        name = artist.name,
        profile = artist.profile.orEmpty(),
        image = artist.image.orEmpty(),
        members = members.map { it.toDomain() }.ifEmpty { null }
    )
}

fun Member.toDomain(): MemberEntity {
    return MemberEntity(
        id = id,
        uuid = uuid,
        name = name,
        imageUrl = imageUrl
    )
}

// ── Albums mappers ────────────────────────────────────────────────────────────
fun List<AlbumWithGenre>.toDomain(pageCache: Pagination): AlbumsDetailEntity {
    return AlbumsDetailEntity(
        pagination = pageCache.toDomain(),
        albums = map { it.toDomain() }
    )
}

fun AlbumWithGenre.toDomain(): AlbumEntity {
    return AlbumEntity(
        id = album.id,
        uuid = album.uuid,
        title = album.title,
        artist = album.artist.orEmpty(),
        year = album.year,
        thumbUrl = album.thumb.orEmpty(),
        format = album.format.orEmpty(),
        label = album.label.orEmpty(),
        genres = genres.map { it.name }
    )
}

fun ArtistReleasesResponse.toDomain(genresByReleaseId: Map<Int, List<String>>): AlbumsDetailEntity {
    return AlbumsDetailEntity(
        pagination = pagination.toDomain(),
        albums = releases.map { it.toDomain(genresByReleaseId[it.id].orEmpty()) }
    )
}

fun ReleaseResponse.toDomain(genres: List<String>): AlbumEntity {
    return AlbumEntity(
        id = id,
        uuid = getUniqueUuid(),
        title = title,
        artist = artist.orEmpty(),
        year = year,
        thumbUrl = thumb.orEmpty(),
        format = format.orEmpty(),
        label = label.orEmpty(),
        genres = genres
    )
}

private fun getUniqueUuid() = UUID.randomUUID().toString()
