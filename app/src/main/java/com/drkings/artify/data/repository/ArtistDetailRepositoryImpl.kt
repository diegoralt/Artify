package com.drkings.artify.data.repository

import com.drkings.artify.data.datasource.api.ApiService
import com.drkings.artify.data.mapper.toDomain
import com.drkings.artify.domain.entity.ArtistDetailEntity
import com.drkings.artify.domain.repository.ArtistDetailRepository
import javax.inject.Inject

class ArtistDetailRepositoryImpl @Inject constructor(val apiService: ApiService) :
    ArtistDetailRepository {

    override suspend fun getDetail(idArtist: Int): ArtistDetailEntity {
        val response = apiService.getArtistDetail(idArtist)

        return response.toDomain()
    }
}