package com.drkings.artify.data.mapper

import com.drkings.artify.data.datasource.api.response.ArtistResponse
import com.drkings.artify.data.datasource.api.response.ArtistReleasesResponse
import com.drkings.artify.data.datasource.api.response.MemberResponse
import com.drkings.artify.data.datasource.api.response.PaginationResponse
import com.drkings.artify.data.datasource.api.response.ReleaseDetailResponse
import com.drkings.artify.data.datasource.api.response.ReleaseResponse
import com.drkings.artify.data.datasource.api.response.SearchResponse
import com.drkings.artify.data.datasource.storage.entity.Album
import com.drkings.artify.data.datasource.storage.entity.AlbumGenreCrossRef
import com.drkings.artify.data.datasource.storage.entity.Artist
import com.drkings.artify.data.datasource.storage.entity.Genre
import com.drkings.artify.data.datasource.storage.entity.Member
import com.drkings.artify.data.datasource.storage.entity.Pagination
import java.util.UUID

// ── Search mappers ───────────────────────────────────────────────────────────

fun SearchResponse.toData(query: String, page: Int): Pair<Pagination, List<Artist>> {
    return Pair(
        first = pagination.toData(query, page),
        second = results.map { artistResponse -> artistResponse.toData(query, page) }
    )
}

fun ArtistResponse.toData(query: String, page: Int): Artist {
    return Artist(
        uuid = getUniqueUuid(),
        id = id,
        type = type,
        name = title,
        thumb = thumb,
        searchQuery = query,
        page = page,
        createdAt = getTimestamp()
    )
}

// ── Pagination mapper ────────────────────────────────────────────────────────

fun PaginationResponse.toData(query: String, page: Int): Pagination {
    return Pagination(
        name = query,
        page = page,
        totalPages = pages,
        totalItems = items,
        createdAt = getTimestamp()
    )
}

// ── Artist detail mappers ────────────────────────────────────────────────────

fun MemberResponse.toData(artistId: Int, thumbnailUrl: String): Member {
    return Member(
        uuid = getUniqueUuid(),
        id = id,
        name = name,
        imageUrl = thumbnailUrl,
        artistId = artistId,
        createdAt = getTimestamp()
    )
}

// ── Release mappers ─────────────────────────────────────────────────────────

fun ArtistReleasesResponse.toData(
    artistId: Int,
    page: Int,
    genresByReleaseId: Map<Int, List<String>>
): Triple<Pagination, List<Album>, List<Genre>> {
    val albums = releases.map { release ->
        release.toData(artistId, page)
    }

    val genres = mutableMapOf<String, Genre>()
    genresByReleaseId.forEach { (releaseId, genreNames) ->
        genreNames.forEach { genreName ->
            val uuid = "${genreName.lowercase()}-genre"
            if (!genres.containsKey(uuid)) {
                genres[uuid] = Genre(
                    uuid = uuid,
                    id = 0,
                    name = genreName,
                    createdAt = getTimestamp()
                )
            }
        }
    }

    return Triple(
        pagination.toData("artist_releases_$artistId", page),
        albums,
        genres.values.toList()
    )
}

fun ReleaseResponse.toData(artistId: Int, page: Int): Album {
    return Album(
        uuid = getUniqueUuid(),
        id = id,
        title = title,
        artist = null,
        year = year,
        thumb = null,
        format = null,
        label = null,
        artistId = artistId,
        page = page,
        createdAt = getTimestamp()
    )
}

fun ReleaseDetailResponse.toGenreCrossRefs(albumUuid: String): List<AlbumGenreCrossRef> {
    return genres?.map { genreName ->
        AlbumGenreCrossRef(
            albumUuid = albumUuid,
            genreUuid = "${genreName.lowercase()}-genre"
        )
    } ?: emptyList()
}

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun getUniqueUuid() = UUID.randomUUID().toString()

private fun getTimestamp() = System.currentTimeMillis()
