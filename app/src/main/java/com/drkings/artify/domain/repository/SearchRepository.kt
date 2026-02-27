package com.drkings.artify.domain.repository

import com.drkings.artify.domain.entity.SearchEntity

interface SearchRepository {
    suspend fun search(query: String, page: Int, perPage: Int): SearchEntity
}
