package com.drkings.artify.data.repository

import com.drkings.artify.data.datasource.api.ApiService
import com.drkings.artify.data.response.ArtistResponse
import com.drkings.artify.data.response.PaginationResponse
import com.drkings.artify.data.response.SearchResponse
import com.drkings.artify.domain.repository.SearchRepository
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.collections.emptyList

class SearchRepositoryImplTest {

    private lateinit var apiService: ApiService
    private lateinit var repository: SearchRepository

    private val page = 1
    private val perPage = 30

    @Before
    fun setUp() {
        apiService = mockk()
        repository = SearchRepositoryImpl(apiService)
    }

    @Test
    fun `search - returns empty list when no results are found`() = runTest {

        // Given
        val query = "nonexistent"

        val fakePaginationResponse = PaginationResponse(
            page = page,
            pages = 1,
            perPage = perPage,
            items = 0
        )
        val fakeResponse = SearchResponse(
            pagination = fakePaginationResponse,
            results = emptyList()
        )
        coEvery { apiService.search(query, page, perPage) } returns fakeResponse

        // When
        val result = repository.search(query, page, perPage)

        // Then
        assertTrue(result.artists.isEmpty())
    }

    @Test
    fun `search - returns artist information when results are found`() = runTest {

        // Given
        val query = "Coldpl"

        val title = "Coldplay"
        val thumb = "https://img.discogs.com/coldplay.jpg"

        val fakePaginationResponse = PaginationResponse(
            page = page,
            pages = 1,
            perPage = perPage,
            items = 0
        )
        val fakeArtistsResponse = listOf(
            ArtistResponse(
                id = 29735,
                type = "artist",
                title = title,
                thumb = thumb
            )
        )

        val fakeResponse = SearchResponse(
            pagination = fakePaginationResponse,
            results = fakeArtistsResponse
        )
        coEvery { apiService.search(query, page, perPage) } returns fakeResponse

        // When
        val result = repository.search(query, page, perPage)

        // Then
        assertEquals(title, result.artists.first().name)
        assertEquals(thumb, result.artists.first().thumbUrl)
    }

    @Test
    fun `search - returns all artist size when results are different artist`() = runTest {

        // Given
        val query = "Coldpl"

        val fakePaginationResponse = PaginationResponse(
            page = page,
            pages = 1,
            perPage = perPage,
            items = 0
        )
        val fakeArtistsResponse = listOf(
            ArtistResponse(
                id = 29735,
                type = "artist",
                title = "Coldplay",
                thumb = "https://img.discogs.com/coldplay.jpg"
            ),
            ArtistResponse(
                id = 29736,
                type = "artist",
                title = "Coldplay Japan",
                thumb = "https://img.discogs.com/coldplay_japan.jpg"
            ),
            ArtistResponse(
                id = 29737,
                type = "artist",
                title = "Coldplay Last",
                thumb = "https://img.discogs.com/coldplay_last.jpg"
            )
        )

        val fakeResponse = SearchResponse(
            pagination = fakePaginationResponse,
            results = fakeArtistsResponse
        )
        coEvery { apiService.search(query, page, perPage) } returns fakeResponse

        // When
        val result = repository.search(query, page, perPage)

        // Then
        assertEquals(fakeArtistsResponse.size, result.artists.size)
    }
}