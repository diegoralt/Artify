package com.drkings.artify.domain.usecase

import com.drkings.artify.domain.entity.ArtistDetailEntity
import com.drkings.artify.domain.entity.MemberEntity
import com.drkings.artify.domain.repository.ArtistDetailRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ArtistDetailUseCaseTest {

    private lateinit var repository: ArtistDetailRepository
    private lateinit var useCase: ArtistDetailUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = ArtistDetailUseCase(repository)
    }

    @Test
    fun `invoke - repository returns entity - emits result success with correct data`() = runTest {
        // Given
        val artistId = 29735
        val fakeArtistDetailEntity = ArtistDetailEntity(
            id = artistId,
            name = "Coldplay",
            profile = "Pop rock band from London.",
            image = "https://img.discogs.com/coldplay.jpg",
            members = listOf(
                MemberEntity(
                    id = 42610,
                    name = "Chris Martin",
                    imageUrl = "https://img.discogs.com/chris.jpg"
                )
            )
        )
        coEvery { repository.getDetail(artistId) } returns fakeArtistDetailEntity

        // When
        val result = useCase(artistId)

        // Then
        assertTrue(result.isSuccess)

        // El repositorio debe haberse llamado exactamente una vez con el ID correcto
        coVerify(exactly = 1) { repository.getDetail(artistId) }
    }

    @Test
    fun `invoke - repository returns entity - emits members when artist has members`() = runTest {
        // Given
        val artistId = 29735
        val fakeArtistDetailEntity = ArtistDetailEntity(
            id = artistId,
            name = "Coldplay",
            profile = "Pop rock band from London.",
            image = "https://img.discogs.com/coldplay.jpg",
            members = listOf(
                MemberEntity(
                    id = 42610,
                    name = "Chris Martin",
                    imageUrl = "https://img.discogs.com/chris.jpg"
                )
            )
        )
        coEvery { repository.getDetail(artistId) } returns fakeArtistDetailEntity

        // When
        val result = useCase(artistId)

        // Then
        assertEquals(1, result.getOrNull()?.members?.size)
    }

    @Test
    fun `invoke - repository throws exception - emits result failure with same exception`() =
        runTest {
            // Given
            val artistId = 29735
            val errorMessage = "Unable to resolve host"
            val networkError = RuntimeException(errorMessage)
            coEvery { repository.getDetail(artistId) } throws networkError

            // When
            val result = useCase(artistId)

            // Then
            assertTrue(result.isFailure)
            assertEquals(errorMessage, result.exceptionOrNull()?.message)
        }
}
