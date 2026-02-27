package com.drkings.artify.domain.usecase

import com.drkings.artify.domain.entity.ArtistEntity
import com.drkings.artify.domain.entity.PaginationEntity
import com.drkings.artify.domain.entity.SearchEntity
import com.drkings.artify.domain.repository.SearchRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SearchUseCaseTest {

    private lateinit var repository: SearchRepository
    private lateinit var useCase: SearchUseCase

    private val query = "Coldplay"
    private val page = 1
    private val perPage = 30

    @Before
    fun setUp() {
        repository = mockk()
        useCase = SearchUseCase(repository)
    }

    @Test
    fun `invoke - repository returns results - emits result success with correct search entity`() =
        runTest {
            // Given
            val fakeSearchEntity = SearchEntity(
                pagination = PaginationEntity(page = 1, pages = 3, items = 58),
                artists = listOf(
                    ArtistEntity(
                        id = 29735,
                        name = "Coldplay",
                        type = "artist",
                        thumbUrl = "https://img.discogs.com/coldplay.jpg"
                    ),
                    ArtistEntity(
                        id = 42610,
                        name = "Chris Martin",
                        type = "artist",
                        thumbUrl = "https://img.discogs.com/chris.jpg"
                    )
                )
            )
            coEvery { repository.search(query, page, perPage) } returns fakeSearchEntity

            // When
            val result = useCase(query, page, perPage)

            // Then
            assertTrue(result.isSuccess)
            assertEquals(fakeSearchEntity, result.getOrNull())

            // El repositorio debe haberse llamado con los parámetros exactos — sin alteraciones
            coVerify(exactly = 1) { repository.search(query, page, perPage) }
        }

    @Test
    fun `invoke - repository returns results - emits artist values with correct search entity`() =
        runTest {
            // Given
            val fakeSearchEntity = SearchEntity(
                pagination = PaginationEntity(page = 1, pages = 3, items = 58),
                artists = listOf(
                    ArtistEntity(
                        id = 29735,
                        name = "Coldplay",
                        type = "artist",
                        thumbUrl = "https://img.discogs.com/coldplay.jpg"
                    ),
                    ArtistEntity(
                        id = 42610,
                        name = "Chris Martin",
                        type = "artist",
                        thumbUrl = "https://img.discogs.com/chris.jpg"
                    )
                )
            )
            coEvery { repository.search(query, page, perPage) } returns fakeSearchEntity

            // When
            val result = useCase(query, page, perPage)

            // Then
            assertEquals(29735, result.getOrNull()?.artists?.first()?.id)
            assertEquals("Coldplay", result.getOrNull()?.artists?.first()?.name)
        }

    @Test
    fun `invoke - repository throws exception - emits result failure with original exception`(): Unit =
        runTest {
            // Given
            val messageError = "No internet connection"

            val networkError = RuntimeException(messageError)
            coEvery { repository.search(query, page, perPage) } throws networkError

            // When
            val result = useCase(query, page, perPage)

            // Then
            assertTrue(result.isFailure)
            assertEquals(messageError, result.exceptionOrNull()?.message)

            // El UseCase no debe reintentar ante errores — una sola llamada
            coVerify(exactly = 1) { repository.search(query, page, perPage) }
        }
}
