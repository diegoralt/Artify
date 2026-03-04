package com.drkings.artify.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.drkings.artify.domain.entity.ArtistDetailEntity
import com.drkings.artify.domain.entity.MemberEntity
import com.drkings.artify.domain.usecase.ArtistDetailUseCase
import com.drkings.artify.presentation.core.ArtistDetail
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArtistDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var artistDetailUseCase: ArtistDetailUseCase
    private lateinit var savedStateHandle: SavedStateHandle

    private val fakeArtist = ArtistDetailEntity(
        id = 29735,
        name = "Coldplay",
        profile = "British rock band formed in London in 1996.",
        image = "https://img.discogs.com/coldplay.jpg",
        members = listOf(
            MemberEntity(
                id = 42610,
                uuid = "f380cc0f-ef08-492b-bf8a-84a8c0d7c9dc",
                name = "Chris Martin",
                imageUrl = ""
            ),
            MemberEntity(
                id = 530745,
                "1d430f16-2d15-4808-b9b0-6d749762b154",
                name = "Guy Berryman",
                imageUrl = ""
            )
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        artistDetailUseCase = mockk()
        mockkStatic("androidx.navigation.SavedStateHandleKt")
        savedStateHandle = mockk<SavedStateHandle>().apply {
            every { toRoute<ArtistDetail>() } returns ArtistDetail(artistId = 29735)
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic("androidx.navigation.SavedStateHandleKt")
    }

    private fun createViewModel() = ArtistDetailViewModel(artistDetailUseCase, savedStateHandle)

    @Test
    fun `init - loading state - uiState doesn't transition to Success until the use case is finished`() =
        runTest {
            // Given
            coEvery { artistDetailUseCase(any()) } returns Result.success(fakeArtist)

            // When
            val viewModel = createViewModel()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is ArtistDetailUiState.Loading)
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value is ArtistDetailUiState.Success)
        }

    @Test
    fun `init - use case succeeds - uiState transitions to Success with correct artist data`() =
        runTest {
            // Given
            coEvery { artistDetailUseCase(any()) } returns Result.success(fakeArtist)

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertEquals(29735, (state as ArtistDetailUiState.Success).artist.id)
            assertEquals(2, state.artist.members?.size)
        }

    @Test
    fun `retry - use case succeeds - uiState recovers to Success after previous failure`() =
        runTest {
            // Given
            coEvery {
                artistDetailUseCase(any())
            } returns Result.failure(RuntimeException("Timeout"))
            val viewModel = createViewModel()
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value is ArtistDetailUiState.Error)

            // When
            coEvery { artistDetailUseCase(any()) } returns Result.success(fakeArtist)
            viewModel.retry()
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is ArtistDetailUiState.Success)
            assertEquals(29735, (state as ArtistDetailUiState.Success).artist.id)
        }
}
