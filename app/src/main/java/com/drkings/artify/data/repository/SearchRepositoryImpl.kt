package com.drkings.artify.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.drkings.artify.data.datasource.api.ApiService
import com.drkings.artify.data.datasource.storage.ArtifyDatabase
import com.drkings.artify.data.mapper.toData
import com.drkings.artify.data.mapper.toDomain
import com.drkings.artify.domain.entity.SearchEntity
import com.drkings.artify.domain.repository.SearchRepository
import javax.inject.Inject

private const val ONE_DAY_MILLIS = 24 * 60 * 60 * 1000L

class SearchRepositoryImpl @Inject constructor(
    val apiService: ApiService,
    val database: ArtifyDatabase
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

                database.withTransaction {
                    artistDao.deleteArtistsByQueryAndPage(query, page)
                    paginationDao.deletePaginationByQueryAndPage(query, page)
                    artistDao.insertArtists(artists)
                    paginationDao.insertPagination(newPageCache)
                }

                artists.toDomain(newPageCache)
            } catch (e: Exception) {
                Log.e("SearchRepositoryImpl", "search: ${e.message}")
                throw e
            }
        } else {
            artistDao.getArtists(query, page, perPage).toDomain(pageCache)
        }
    }
}
