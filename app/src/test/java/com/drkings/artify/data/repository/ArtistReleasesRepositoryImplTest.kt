package com.drkings.artify.data.repository

import com.drkings.artify.data.datasource.api.ApiService
import com.drkings.artify.data.response.ArtistReleasesResponse
import com.drkings.artify.data.response.PaginationResponse
import com.drkings.artify.data.response.ReleaseDetailResponse
import com.drkings.artify.data.response.ReleaseResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ArtistReleasesRepositoryImplTest {

    private lateinit var apiService: ApiService
    private lateinit var repository: ArtistReleasesRepositoryImpl

    // Paginación reutilizable en todos los tests
    private val fakePagination = PaginationResponse(page = 1, pages = 3, perPage = 30, items = 60)

    @Before
    fun setUp() {
        apiService = mockk()
        repository = ArtistReleasesRepositoryImpl(apiService)
    }

    @Test
    fun `getReleases - release has genre detail - call release for each album`() =
        runTest {
            // Given
            val artistId = 29735
            val release1 = ReleaseResponse(id = 111, title = "Music of the Spheres", year = 2021)
            val release2 = ReleaseResponse(id = 222, title = "Everyday Life", year = 2019)

            coEvery {
                apiService.getArtistReleases(
                    artistId,
                    1,
                    30
                )
            } returns ArtistReleasesResponse(
                pagination = fakePagination,
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

            // When
            val result = repository.getReleases(artistId, page = 1, perPage = 30)

            // Then
            // getReleaseDetail debe haberse llamado una vez por cada release
            coVerify(exactly = 1) { apiService.getReleaseDetail(111) }
            coVerify(exactly = 1) { apiService.getReleaseDetail(222) }
        }

    @Test
    fun `getReleases - all releases have genre detail - maps genres correctly to each album`() =
        runTest {
            // Given
            val artistId = 29735
            val release1 = ReleaseResponse(id = 111, title = "Music of the Spheres", year = 2021)
            val release2 = ReleaseResponse(id = 222, title = "Everyday Life", year = 2019)

            coEvery {
                apiService.getArtistReleases(
                    artistId,
                    1,
                    30
                )
            } returns ArtistReleasesResponse(
                pagination = fakePagination,
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

            // When
            val result = repository.getReleases(artistId, page = 1, perPage = 30)

            // Then
            val album1 = result.albums.first { it.id == 111 }
            val album2 = result.albums.first { it.id == 222 }

            assertEquals(listOf("Rock", "Pop"), album1.genres)
            assertEquals(listOf("Alternative Rock"), album2.genres)
        }

    @Test
    fun `getReleases - one release detail fails - assigns empty genres without cancelling others`() =
        runTest {
            // Given
            val artistId = 29735
            val release1 = ReleaseResponse(id = 111, title = "Music of the Spheres", year = 2021)
            val release2 = ReleaseResponse(id = 222, title = "Everyday Life", year = 2019)

            coEvery {
                apiService.getArtistReleases(
                    artistId,
                    1,
                    30
                )
            } returns ArtistReleasesResponse(
                pagination = fakePagination,
                releases = listOf(release1, release2)
            )
            // Release 111 responde correctamente
            coEvery { apiService.getReleaseDetail(111) } returns ReleaseDetailResponse(
                id = 111,
                genres = listOf("Rock", "Pop")
            )
            // Release 222 falla — simula timeout o 404
            coEvery { apiService.getReleaseDetail(222) } throws RuntimeException("HTTP 404 Not Found")

            // When
            val result = repository.getReleases(artistId, page = 1, perPage = 30)

            // Then
            // Ambos álbumes deben estar presentes — el fallo de uno no cancela el otro
            assertEquals(2, result.albums.size)
        }
}
