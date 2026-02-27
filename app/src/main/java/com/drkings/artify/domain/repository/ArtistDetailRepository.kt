package com.drkings.artify.domain.repository

import com.drkings.artify.domain.entity.ArtistDetailEntity

interface ArtistDetailRepository {
    suspend fun getDetail(artistId: Int): ArtistDetailEntity
}
