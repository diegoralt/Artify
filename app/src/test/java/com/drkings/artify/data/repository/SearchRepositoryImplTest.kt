package com.drkings.artify.data.repository

import com.drkings.artify.data.datasource.api.ApiService
import com.drkings.artify.data.datasource.api.response.ArtistResponse
import com.drkings.artify.data.datasource.api.response.PaginationResponse
import com.drkings.artify.data.datasource.api.response.SearchResponse
import com.drkings.artify.data.datasource.storage.ArtifyDatabase
import com.drkings.artify.data.datasource.storage.ArtistDao
import com.drkings.artify.data.datasource.storage.PaginationDao
import com.drkings.artify.data.datasource.storage.entity.Artist
import com.drkings.artify.data.datasource.storage.entity.Pagination
import com.drkings.artify.domain.repository.SearchRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SearchRepositoryImplTest {

    private lateinit var apiService: ApiService
    private lateinit var database: ArtifyDatabase
    private lateinit var artistDao: ArtistDao
    private lateinit var paginationDao: PaginationDao
    private lateinit var repository: SearchRepository

    private val page = 1
    private val perPage = 30

    @Before
    fun setUp() {
        apiService = mockk()
        database = mockk()
        artistDao = mockk()
        paginationDao = mockk()

        every { database.artistDao() } returns artistDao
        every { database.paginationDao() } returns paginationDao

        // Cache vacío por defecto → siempre consulta el servicio
        coEvery { paginationDao.getPagination(any(), any()) } returns null
        coEvery { artistDao.deleteArtistsByQueryAndPage(any(), any()) } just Runs
        coEvery { paginationDao.deletePaginationByQueryAndPage(any(), any()) } just Runs
        coEvery { artistDao.insertArtists(any()) } just Runs
        coEvery { paginationDao.insertPagination(any()) } just Runs

        repository = SearchRepositoryImpl(
            apiService = apiService,
            database = database,
            transactionRunner = { block -> block() }
        )
    }

    // ── Tests sin caché ─────────────────────────────────────────────
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

    // ── Tests con caché ─────────────────────────────────────────────
    @Test
    fun `search - clears database and fetches from api when cache is expired`() = runTest {
        // Given
        val query = "Coldplay"
        val expiredCreatedAt = System.currentTimeMillis() - (25 * 60 * 60 * 1000L) // hace 25 horas

        val expiredPagination = Pagination(
            name = query,
            page = page,
            totalPages = 1,
            totalItems = 1,
            createdAt = expiredCreatedAt
        )
        coEvery { paginationDao.getPagination(query, page) } returns expiredPagination

        val fakePaginationResponse = PaginationResponse(
            page = page,
            pages = 1,
            perPage = perPage,
            items = 30
        )
        val fakeResponse = SearchResponse(
            pagination = fakePaginationResponse,
            results = listOf(
                ArtistResponse(id = 1, type = "artist", title = "Coldplay", thumb = "url")
            )
        )
        coEvery { apiService.search(query, page, perPage) } returns fakeResponse

        // When
        repository.search(query, page, perPage)

        // Then
        coVerify(exactly = 1) { artistDao.deleteArtistsByQueryAndPage(query, page) }
        coVerify(exactly = 1) { paginationDao.deletePaginationByQueryAndPage(query, page) }

        coVerify(exactly = 1) { apiService.search(query, page, perPage) }
    }

    @Test
    fun `search - returns cached artists from database matching the search query`() = runTest {
        // Given
        val query = "Coldpl"
        val createdAt = System.currentTimeMillis() // caché reciente, no expirado

        val freshPagination = Pagination(
            name = query,
            page = page,
            totalPages = 1,
            totalItems = 2,
            createdAt = createdAt
        )
        val cachedArtists = listOf(
            Artist(
                uuid = "09e8638b-a98a-40da-8506-ae1c368492b9",
                id = 29735,
                type = "artist",
                name = "Coldplay",
                thumb = "https://img.discogs.com/coldplay.jpg",
                searchQuery = query,
                page = page,
                createdAt = createdAt
            ),
            Artist(
                uuid = "f380cc0f-ef08-492b-bf8a-84a8c0d7c9dc",
                id = 29736,
                type = "artist",
                name = "Coldplay Japan",
                thumb = "https://img.discogs.com/coldplay_japan.jpg",
                searchQuery = query,
                page = page,
                createdAt = createdAt
            )
        )

        coEvery { paginationDao.getPagination(query, page) } returns freshPagination
        coEvery { artistDao.getArtists(query, page, perPage) } returns cachedArtists

        // When
        val result = repository.search(query, page, perPage)

        // Then
        coVerify(exactly = 0) { apiService.search(any(), any(), any()) }

        assertEquals(cachedArtists.first().name, result.artists.first().name)
        assertEquals(cachedArtists.last().name, result.artists.last().name)
    }

    @Test
    fun `search - returns number of artists matching perPage from database when cache is valid`() =
        runTest {
            // Given
            val query = "Coldpl"
            val createdAt = System.currentTimeMillis() // caché reciente, no expirado

            val freshPagination = Pagination(
                name = query,
                page = page,
                totalPages = 1,
                totalItems = 2,
                createdAt = createdAt
            )
            val cachedArtists = List(perPage) { index ->
                Artist(
                    uuid = "09e8638b-a98a-40da-8506-ae1c368492b9",
                    id = index,
                    type = "artist",
                    name = "Coldplay $index",
                    thumb = "https://img.discogs.com/coldplay_$index.jpg",
                    searchQuery = query,
                    page = page,
                    createdAt = createdAt
                )
            }

            coEvery { paginationDao.getPagination(query, page) } returns freshPagination
            coEvery { artistDao.getArtists(query, page, perPage) } returns cachedArtists

            // When
            val result = repository.search(query, page, perPage)

            // Then
            coVerify(exactly = 0) { apiService.search(any(), any(), any()) }
            assertEquals(perPage, result.artists.size)
        }
}
