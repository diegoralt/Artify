package com.drkings.artify.data.mapper

import com.drkings.artify.data.datasource.api.response.ArtistResponse
import com.drkings.artify.data.datasource.api.response.SearchResponse
import com.drkings.artify.data.datasource.storage.entity.Artist
import java.util.UUID

// ── Artist mappers ───────────────────────────────────────────────────────────
fun SearchResponse.toData(): List<Artist> {
    return results.map { artistResponse -> artistResponse.toData() }
}

fun ArtistResponse.toData(): Artist {
    return Artist(
        uuid = getUniqueUuid(),
        id = id,
        type = type,
        name = title,
        thumb = thumb,
        createdAt = getTimestamp()
    )
}

private fun getUniqueUuid() = UUID.randomUUID().toString()

private fun getTimestamp() = System.currentTimeMillis()