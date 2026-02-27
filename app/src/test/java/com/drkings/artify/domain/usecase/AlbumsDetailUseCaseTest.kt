package com.drkings.artify.domain.usecase

import com.drkings.artify.domain.entity.AlbumEntity
import com.drkings.artify.domain.entity.AlbumsDetailEntity
import com.drkings.artify.domain.entity.PaginationEntity
import com.drkings.artify.domain.repository.ArtistReleasesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AlbumsDetailUseCaseTest {

    private lateinit var repository: ArtistReleasesRepository
    private lateinit var useCase: AlbumsDetailUseCase

    private val page = 1
    private val perPage = 30

    @Before
    fun setUp() {
        repository = mockk()
        useCase = AlbumsDetailUseCase(repository)
    }

    @Test
    fun `invoke - repository returns entity - emits result success with correct albums size`() =
        runTest {
            // Given
            val artistId = 29735

            val fakeAlbumsDetailEntity = AlbumsDetailEntity(
                pagination = PaginationEntity(page = 1, pages = 5, items = 142),
                albums = listOf(
                    AlbumEntity(
                        id = 123456,
                        title = "Music of the Spheres",
                        artist = "Coldplay",
                        year = 2021,
                        thumbUrl = "https://img.discogs.com/spheres.jpg",
                        format = "Album",
                        label = "Parlophone",
                        genres = listOf("Rock", "Pop")
                    ),
                    AlbumEntity(
                        id = 789012,
                        title = "Everyday Life",
                        artist = "Coldplay",
                        year = 2019,
                        thumbUrl = "https://img.discogs.com/everyday.jpg",
                        format = "Album",
                        label = "Parlophone",
                        genres = listOf("Rock")
                    )
                )
            )
            coEvery {
                repository.getReleases(
                    artistId,
                    page,
                    perPage
                )
            } returns fakeAlbumsDetailEntity

            // When
            val result = useCase(artistId, page, perPage)

            // Then
            assertTrue(result.isSuccess)
            assertEquals(2, result.getOrNull()?.albums?.size)

            // El repositorio debe haberse llamado exactamente una vez con los parámetros correctos
            coVerify(exactly = 1) { repository.getReleases(artistId, page, perPage) }
        }

    @Test
    fun `invoke - repository returns entity - emits result success with correct genres size`() =
        runTest {
            // Given
            val artistId = 29735

            val fakeAlbumsDetailEntity = AlbumsDetailEntity(
                pagination = PaginationEntity(page = 1, pages = 5, items = 142),
                albums = listOf(
                    AlbumEntity(
                        id = 123456,
                        title = "Music of the Spheres",
                        artist = "Coldplay",
                        year = 2021,
                        thumbUrl = "https://img.discogs.com/spheres.jpg",
                        format = "Album",
                        label = "Parlophone",
                        genres = listOf("Rock", "Pop", "Alternative")
                    )
                )
            )
            coEvery {
                repository.getReleases(
                    artistId,
                    page,
                    perPage
                )
            } returns fakeAlbumsDetailEntity

            // When
            val result = useCase(artistId, page, perPage)

            // Then
            assertEquals(3, result.getOrNull()?.albums?.first()?.genres?.size)
        }

    @Test
    fun `invoke - repository throws exception - emits result failure with original message exception`() =
        runTest {
            // Given
            val artistId = 29735
            val messageError = "HTTP 429 Too Many Requests"
            val rateLimitError = RuntimeException(messageError)
            coEvery { repository.getReleases(artistId, page, perPage) } throws rateLimitError

            // When
            val result = useCase(artistId, page, perPage)

            // Then
            assertTrue(result.isFailure)
            assertEquals(messageError, result.exceptionOrNull()?.message)
        }
}
