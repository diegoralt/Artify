package com.drkings.artify.domain.usecase

import com.drkings.artify.domain.entity.AlbumsDetailEntity
import com.drkings.artify.domain.repository.ArtistReleasesRepository
import javax.inject.Inject

class AlbumsDetailUseCase @Inject constructor(private val artistReleasesRepository: ArtistReleasesRepository) {
    suspend operator fun invoke(
        artistId: Int,
        page: Int,
        perPage: Int
    ): Result<AlbumsDetailEntity> {
        return runCatching {
            artistReleasesRepository.getReleases(artistId, page, perPage)
        }
    }
}