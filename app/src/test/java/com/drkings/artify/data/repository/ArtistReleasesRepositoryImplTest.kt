package com.drkings.artify.data.repository

import com.drkings.artify.data.datasource.api.ApiService
import com.drkings.artify.data.datasource.api.response.ArtistReleasesResponse
import com.drkings.artify.data.datasource.api.response.PaginationResponse
import com.drkings.artify.data.datasource.api.response.ReleaseDetailResponse
import com.drkings.artify.data.datasource.api.response.ReleaseResponse
import com.drkings.artify.data.datasource.storage.ArtifyDatabase
import com.drkings.artify.data.datasource.storage.ReleaseDao
import com.drkings.artify.data.datasource.storage.PaginationDao
import com.drkings.artify.data.datasource.storage.entity.Album
import com.drkings.artify.data.datasource.storage.entity.AlbumGenreCrossRef
import com.drkings.artify.data.datasource.storage.entity.AlbumWithGenre
import com.drkings.artify.data.datasource.storage.entity.Genre
import com.drkings.artify.data.datasource.storage.entity.Pagination
import com.drkings.artify.domain.repository.ArtistReleasesRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ArtistReleasesRepositoryImplTest {

    private lateinit var apiService: ApiService
    private lateinit var database: ArtifyDatabase
    private lateinit var releaseDao: ReleaseDao
    private lateinit var paginationDao: PaginationDao
    private lateinit var repository: ArtistReleasesRepository

    private val page = 1
    private val perPage = 30

    @Before
    fun setUp() {
        apiService = mockk()
        database = mockk()
        releaseDao = mockk()
        paginationDao = mockk()

        every { database.releaseDao() } returns releaseDao
        every { database.paginationDao() } returns paginationDao

        coEvery { paginationDao.getPagination(any(), any()) } returns null
        coEvery { releaseDao.deleteAlbumsByArtistAndPage(any(), any()) } just Runs
        coEvery { releaseDao.deleteGenreRelationsByArtistAndPage(any(), any()) } just Runs
        coEvery { releaseDao.insertAlbums(any()) } just Runs
        coEvery { releaseDao.insertGenres(any()) } just Runs
        coEvery { releaseDao.insertAlbumGenreCrossRefs(any()) } just Runs
        coEvery { paginationDao.insertPagination(any()) } just Runs

        repository = ArtistReleasesRepositoryImpl(
            apiService = apiService,
            database = database
        )
    }

    // ── Tests sin caché ─────────────────────────────────────────────────────

    @Test
    fun `getReleases - no cache - fetches from api and stores in database`() = runTest {
        // Given
        val artistId = 29735
        val release1 = ReleaseResponse(id = 111, title = "Music of the Spheres", year = 2021)
        val release2 = ReleaseResponse(id = 222, title = "Everyday Life", year = 2019)

        val fakePaginationResponse = PaginationResponse(page = page, pages = 3, perPage = perPage, items = 60)
        coEvery {
            apiService.getArtistReleases(
                artistId,
                page,
                perPage
            )
        } returns ArtistReleasesResponse(
            pagination = fakePaginationResponse,
            releases = listOf(release1, release2)
        )
        coEvery { apiService.getReleaseDetail(111) } returns ReleaseDetailResponse(
            id = 111,
            genres = listOf("Rock", "Pop")
        )
        coEvery { apiService.getReleaseDetail(222) } returns ReleaseDetailResponse(
            id = 222,
            genres = listOf("Alternative Rock")
        )

        val cachedAlbums = listOf(
            AlbumWithGenre(
                album = Album(
                    uuid = "album-111",
                    id = 111,
                    title = "Music of the Spheres",
                    artist = null,
                    year = 2021,
                    thumb = null,
                    format = null,
                    label = null,
                    artistId = artistId,
                    page = page,
                    createdAt = System.currentTimeMillis()
                ),
                genres = listOf(
                    Genre(uuid = "rock-genre", id = 0, name = "Rock", createdAt = 0),
                    Genre(uuid = "pop-genre", id = 0, name = "Pop", createdAt = 0)
                )
            )
        )
        coEvery { releaseDao.getAlbumsWithGenres(artistId, page) } returns cachedAlbums

        // When
        val result = repository.getReleases(artistId, page, perPage)

        // Then
        coEvery { apiService.getArtistReleases(artistId, page, perPage) }
        coEvery { apiService.getReleaseDetail(111) }
        coEvery { apiService.getReleaseDetail(222) }
        coVerify(exactly = 1) { releaseDao.deleteAlbumsByArtistAndPage(artistId, page) }
        coVerify(exactly = 1) { releaseDao.insertAlbums(any()) }
        coVerify(exactly = 1) { releaseDao.insertGenres(any()) }
    }

    // ── Tests con caché ─────────────────────────────────────────────────────

    @Test
    fun `getReleases - valid cache - returns cached data without api call`() = runTest {
        // Given
        val artistId = 29735
        val createdAt = System.currentTimeMillis()
        val cacheKey = "artist_releases_$artistId"

        val freshPagination = Pagination(
            name = cacheKey,
            page = page,
            totalPages = 1,
            totalItems = 1,
            createdAt = createdAt
        )

        val cachedAlbums = listOf(
            AlbumWithGenre(
                album = Album(
                    uuid = "album-111",
                    id = 111,
                    title = "Music of the Spheres",
                    artist = null,
                    year = 2021,
                    thumb = null,
                    format = null,
                    label = null,
                    artistId = artistId,
                    page = page,
                    createdAt = createdAt
                ),
                genres = listOf(
                    Genre(uuid = "rock-genre", id = 0, name = "Rock", createdAt = createdAt)
                )
            )
        )

        coEvery { paginationDao.getPagination(cacheKey, page) } returns freshPagination
        coEvery { releaseDao.getAlbumsWithGenres(artistId, page) } returns cachedAlbums

        // When
        val result = repository.getReleases(artistId, page, perPage)

        // Then
        coVerify(exactly = 0) { apiService.getArtistReleases(any(), any(), any()) }
        assertEquals(1, result.albums.size)
        assertEquals("Music of the Spheres", result.albums.first().title)
        assertEquals(listOf("Rock"), result.albums.first().genres)
    }

    @Test
    fun `getReleases - expired cache - refreshes data from api`() = runTest {
        // Given
        val artistId = 29735
        val expiredCreatedAt = System.currentTimeMillis() - (25 * 60 * 60 * 1000L) // 25 horas atrás
        val cacheKey = "artist_releases_$artistId"

        val expiredPagination = Pagination(
            name = cacheKey,
            page = page,
            totalPages = 1,
            totalItems = 1,
            createdAt = expiredCreatedAt
        )

        val release1 = ReleaseResponse(id = 111, title = "Music of the Spheres", year = 2021)
        val fakePaginationResponse = PaginationResponse(page = page, pages = 1, perPage = perPage, items = 30)

        coEvery { paginationDao.getPagination(cacheKey, page) } returns expiredPagination
        coEvery { apiService.getArtistReleases(artistId, page, perPage) } returns ArtistReleasesResponse(
            pagination = fakePaginationResponse,
            releases = listOf(release1)
        )
        coEvery { apiService.getReleaseDetail(111) } returns ReleaseDetailResponse(
            id = 111,
            genres = listOf("Rock", "Pop")
        )

        val newCachedAlbums = listOf(
            AlbumWithGenre(
                album = Album(
                    uuid = "album-111-new",
                    id = 111,
                    title = "Music of the Spheres",
                    artist = null,
                    year = 2021,
                    thumb = null,
                    format = null,
                    label = null,
                    artistId = artistId,
                    page = page,
                    createdAt = System.currentTimeMillis()
                ),
                genres = listOf(
                    Genre(uuid = "rock-genre", id = 0, name = "Rock", createdAt = 0),
                    Genre(uuid = "pop-genre", id = 0, name = "Pop", createdAt = 0)
                )
            )
        )
        coEvery { releaseDao.getAlbumsWithGenres(artistId, page) } returns newCachedAlbums

        // When
        val result = repository.getReleases(artistId, page, perPage)

        // Then
        coVerify(exactly = 1) { apiService.getArtistReleases(artistId, page, perPage) }
        coVerify(exactly = 1) { releaseDao.deleteAlbumsByArtistAndPage(artistId, page) }
        coVerify(exactly = 1) { releaseDao.insertAlbums(any()) }
    }
}
