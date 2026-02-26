package com.drkings.artify.data.repository

import com.drkings.artify.data.datasource.api.ApiService
import com.drkings.artify.data.mapper.toDomain
import com.drkings.artify.domain.entity.SearchEntity
import com.drkings.artify.domain.repository.SearchRepository
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(val apiService: ApiService) : SearchRepository {
    override suspend fun search(query: String, page: Int, perPage: Int): SearchEntity {
        val response = apiService.search(query, page, perPage)

        return response.toDomain()
    }
}