package com.drkings.artify.domain.repository

import com.drkings.artify.domain.entity.AlbumsDetailEntity

interface ArtistReleasesRepository {
    suspend fun getReleases(
        artistId: Int,
        page: Int,
        perPage: Int
    ): AlbumsDetailEntity
}