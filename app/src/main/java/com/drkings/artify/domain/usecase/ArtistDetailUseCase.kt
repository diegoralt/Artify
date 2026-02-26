package com.drkings.artify.domain.usecase

import com.drkings.artify.domain.entity.ArtistDetailEntity
import com.drkings.artify.domain.repository.ArtistDetailRepository
import javax.inject.Inject

class ArtistDetailUseCase @Inject constructor(private val artistDetailRepository: ArtistDetailRepository) {
    suspend operator fun invoke(idArtist: Int): Result<ArtistDetailEntity> {
        return runCatching {
            artistDetailRepository.getDetail(idArtist)
        }
    }
}