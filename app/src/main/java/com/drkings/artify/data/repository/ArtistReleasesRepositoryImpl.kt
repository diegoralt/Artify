package com.drkings.artify.data.repository

import android.util.Log
import com.drkings.artify.data.datasource.api.ApiService
import com.drkings.artify.data.datasource.storage.ArtifyDatabase
import com.drkings.artify.data.mapper.toData
import com.drkings.artify.data.mapper.toDomain
import com.drkings.artify.domain.entity.AlbumsDetailEntity
import com.drkings.artify.domain.repository.ArtistReleasesRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

private const val ONE_DAY_MILLIS = 24 * 60 * 60 * 1000L

class ArtistReleasesRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val database: ArtifyDatabase
) : ArtistReleasesRepository {

    override suspend fun getReleases(artistId: Int, page: Int, perPage: Int): AlbumsDetailEntity {
        val releaseDao = database.releaseDao()
        val paginationDao = database.paginationDao()

        val cacheKey = "artist_releases_$artistId"
        val pageCache = paginationDao.getPagination(cacheKey, page)
        val isExpired = pageCache != null &&
            (System.currentTimeMillis() - pageCache.createdAt) >= ONE_DAY_MILLIS

        return if (pageCache == null || isExpired) {
            try {
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

                    val (newPageCache, albums, genres) = response.toData(artistId, page, genresByReleaseId)
                    val genreCrossRefs = albums.flatMap { album ->
                        val releaseGenres = genresByReleaseId[album.id].orEmpty()
                        releaseGenres.map { genreName ->
                            com.drkings.artify.data.datasource.storage.entity.AlbumGenreCrossRef(
                                albumUuid = album.uuid,
                                genreUuid = "${genreName.lowercase()}-genre"
                            )
                        }
                    }

                    releaseDao.deleteAlbumsByArtistAndPage(artistId, page)
                    releaseDao.deleteGenreRelationsByArtistAndPage(artistId, page)
                    releaseDao.insertAlbums(albums)
                    releaseDao.insertGenres(genres)
                    releaseDao.insertAlbumGenreCrossRefs(genreCrossRefs)
                    paginationDao.insertPagination(newPageCache)

                    releaseDao.getAlbumsWithGenres(artistId, page).toDomain(newPageCache)
                }
            } catch (e: Exception) {
                Log.e("ArtistReleasesRepositoryImpl", "getReleases: API failed with ${e.message}, attempting offline fallback")
                // Fallback: usar caché aunque esté expirado si hay disponible
                if (pageCache != null) {
                    Log.i("ArtistReleasesRepositoryImpl", "Using expired cache for artistId=$artistId, page=$page")
                    releaseDao.getAlbumsWithGenres(artistId, page).toDomain(pageCache)
                } else {
                    Log.e("ArtistReleasesRepositoryImpl", "No cache available for artistId=$artistId, page=$page")
                    throw e
                }
            }
        } else {
            releaseDao.getAlbumsWithGenres(artistId, page).toDomain(pageCache)
        }
    }
}
