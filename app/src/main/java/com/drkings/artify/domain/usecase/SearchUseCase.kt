package com.drkings.artify.domain.usecase

import com.drkings.artify.domain.entity.SearchEntity
import com.drkings.artify.domain.repository.SearchRepository
import javax.inject.Inject

class SearchUseCase @Inject constructor(val searchRepository: SearchRepository) {
    suspend operator fun invoke(query: String, page: Int, perPage: Int): SearchEntity {
        return searchRepository.search(query, page, perPage)

    }
}