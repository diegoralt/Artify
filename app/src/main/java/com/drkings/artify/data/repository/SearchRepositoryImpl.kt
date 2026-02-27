package com.drkings.artify.data.repository

import android.util.Log
import com.drkings.artify.data.datasource.api.ApiService
import com.drkings.artify.data.mapper.toDomain
import com.drkings.artify.domain.entity.SearchEntity
import com.drkings.artify.domain.repository.SearchRepository
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(val apiService: ApiService) : SearchRepository {
    override suspend fun search(query: String, page: Int, perPage: Int): SearchEntity {
        return try {
            val response = apiService.search(query, page, perPage)

            response.toDomain()
        } catch (e: Exception) {
            Log.e("SearchRepositoryImpl", "search: ${e.message}")
            throw e
        }
    }
}
