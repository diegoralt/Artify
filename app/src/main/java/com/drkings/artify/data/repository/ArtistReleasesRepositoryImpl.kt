package com.drkings.artify.data.repository

import android.util.Log
import com.drkings.artify.data.datasource.api.ApiService
import com.drkings.artify.data.mapper.toDomain
import com.drkings.artify.domain.entity.AlbumsDetailEntity
import com.drkings.artify.domain.repository.ArtistReleasesRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class ArtistReleasesRepositoryImpl @Inject constructor(val apiService: ApiService) :
    ArtistReleasesRepository {
    override suspend fun getReleases(artistId: Int, page: Int, perPage: Int): AlbumsDetailEntity {
        return try {
            coroutineScope {
                val response = apiService.getArtistReleases(artistId, page, perPage)

                val genresByReleaseId: Map<Int, List<String>> = response.releases.map { release ->
                    async {
                        val detail = runCatching {
                            apiService.getReleaseDetail(release.id)
                        }.getOrNull()

                        release.id to (detail?.genres.orEmpty())
                    }
                }.awaitAll().toMap()

                response.toDomain(genresByReleaseId)
            }
        } catch (e: Exception) {
            Log.e("ArtistReleasesRepositoryImpl", "getReleases: ${e.message}")
            throw e
        }
    }
}
