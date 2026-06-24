package com.drkings.artify.data.repository

import android.util.Log
import com.drkings.artify.data.datasource.api.ApiService
import com.drkings.artify.data.datasource.storage.ArtifyDatabase
import com.drkings.artify.data.mapper.toData
import com.drkings.artify.data.mapper.toDomain
import com.drkings.artify.domain.entity.SearchEntity
import com.drkings.artify.domain.repository.SearchRepository

private const val ONE_DAY_MILLIS = 24 * 60 * 60 * 1000L

typealias TransactionRunner = suspend (block: suspend () -> Unit) -> Unit

class SearchRepositoryImpl(
    private val apiService: ApiService,
    private val database: ArtifyDatabase,
    private val transactionRunner: TransactionRunner
) : SearchRepository {

    override suspend fun search(query: String, page: Int, perPage: Int): SearchEntity {
        val artistDao = database.artistDao()
        val paginationDao = database.paginationDao()

        val pageCache = paginationDao.getPagination(query, page)
        val isExpired = pageCache != null &&
            (System.currentTimeMillis() - pageCache.createdAt) >= ONE_DAY_MILLIS

        return if (pageCache == null || isExpired) {
            try {
                val response = apiService.search(query, page, perPage)
                val (newPageCache, artists) = response.toData(query, page)

                transactionRunner {
                    artistDao.deleteArtistsByQueryAndPage(query, page)
                    paginationDao.deletePaginationByQueryAndPage(query, page)
                    artistDao.insertArtists(artists)
                    paginationDao.insertPagination(newPageCache)
                }

                artists.toDomain(newPageCache)
            } catch (e: Exception) {
                Log.e("SearchRepositoryImpl", "search: API failed with ${e.message}, attempting offline fallback")
                // Fallback: usar caché aunque esté expirado si hay disponible
                if (pageCache != null) {
                    Log.i("SearchRepositoryImpl", "Using expired cache for query=$query, page=$page")
                    artistDao.getArtists(query, page, perPage).toDomain(pageCache)
                } else {
                    Log.e("SearchRepositoryImpl", "No cache available for query=$query, page=$page")
                    throw e
                }
            }
        } else {
            artistDao.getArtists(query, page, perPage).toDomain(pageCache)
        }
    }
}
