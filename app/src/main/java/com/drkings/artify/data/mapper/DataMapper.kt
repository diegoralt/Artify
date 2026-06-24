package com.drkings.artify.data.mapper

import com.drkings.artify.data.datasource.api.response.ArtistResponse
import com.drkings.artify.data.datasource.api.response.MemberResponse
import com.drkings.artify.data.datasource.api.response.PaginationResponse
import com.drkings.artify.data.datasource.api.response.SearchResponse
import com.drkings.artify.data.datasource.storage.entity.Artist
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

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun getUniqueUuid() = UUID.randomUUID().toString()

private fun getTimestamp() = System.currentTimeMillis()
