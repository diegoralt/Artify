package com.drkings.artify.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.drkings.artify.domain.entity.AlbumEntity
import com.drkings.artify.domain.entity.AlbumsDetailEntity
import com.drkings.artify.domain.entity.PaginationEntity
import com.drkings.artify.domain.usecase.AlbumsDetailUseCase
import com.drkings.artify.presentation.core.AlbumsDetail
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
class AlbumsDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var albumsDetailUseCase: AlbumsDetailUseCase
    private lateinit var savedStateHandle: SavedStateHandle

    private val fakeAlbums = listOf(
        AlbumEntity(
            id = 1,
            title = "Parachutes",
            artist = "Coldplay",
            year = 2000,
            thumbUrl = "",
            format = "Album",
            label = "Parlophone",
            genres = listOf("Rock", "Alternative")
        ),
        AlbumEntity(
            id = 2,
            title = "A Rush of Blood to the Head",
            artist = "Coldplay",
            year = 2002,
            thumbUrl = "",
            format = "Album",
            label = "Atlantic",
            genres = listOf("Rock", "Alternative")
        ),
        AlbumEntity(
            id = 3,
            title = "Music of the Spheres",
            artist = "Coldplay",
            year = 2021,
            thumbUrl = "",
            format = "Album",
            label = "Parlophone",
            genres = listOf("Pop")
        )
    )

    private val fakeAlbumsDetailEntity = AlbumsDetailEntity(
        pagination = PaginationEntity(page = 1, pages = 1, items = 30),
        albums = fakeAlbums
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        albumsDetailUseCase = mockk()
        mockkStatic("androidx.navigation.SavedStateHandleKt")
        savedStateHandle = mockk<SavedStateHandle>().apply {
            every { toRoute<AlbumsDetail>() } returns AlbumsDetail(
                artistId = 29735,
                artistName = "Coldplay"
            )
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic("androidx.navigation.SavedStateHandleKt")
    }

    private fun createViewModel() = AlbumsDetailViewModel(albumsDetailUseCase, savedStateHandle)

    @Test
    fun `init - use case succeeds - uiState transitions to Success with all albums loaded`() =
        runTest {
            // Given
            coEvery {
                albumsDetailUseCase(any(), any(), any())
            } returns Result.success(fakeAlbumsDetailEntity)

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is AlbumsUiState.Success)
            assertEquals(3, (state as AlbumsUiState.Success).albums.size)
        }

    @Test
    fun `init - use case fails - uiState transitions to Error`() =
        runTest {
            // Given
            coEvery {
                albumsDetailUseCase(any(), any(), any())
            } returns Result.failure(RuntimeException("Network error"))

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            assertTrue(viewModel.uiState.value is AlbumsUiState.Error)
        }

    @Test
    fun `toggleYear - year filter applied - only albums matching that year are shown`() =
        runTest {
            // Given
            coEvery {
                albumsDetailUseCase(any(), any(), any())
            } returns Result.success(fakeAlbumsDetailEntity)
            val viewModel = createViewModel()
            advanceUntilIdle()

            // When
            viewModel.toggleYear(2021)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value as AlbumsUiState.Success
            assertEquals(1, state.albums.size)
            assertEquals(3, state.albums.first().id)
        }

    @Test
    fun `clearFilters - active year filter cleared - all albums are visible again`() =
        runTest {
            // Given
            coEvery {
                albumsDetailUseCase(any(), any(), any())
            } returns Result.success(fakeAlbumsDetailEntity)
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.toggleYear(2021)
            advanceUntilIdle()

            // When
            viewModel.clearFilters()
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value as AlbumsUiState.Success
            assertEquals(3, state.albums.size)
            assertTrue(state.filterState.isActive.not())
        }

    @Test
    fun `setSortOrder - OLDEST_FIRST applied - albums are ordered by year ascending`() =
        runTest {
            // Given
            coEvery {
                albumsDetailUseCase(any(), any(), any())
            } returns Result.success(fakeAlbumsDetailEntity)
            val viewModel = createViewModel()
            advanceUntilIdle()

            // When
            viewModel.setSortOrder(SortOrder.OLDEST_FIRST)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value as AlbumsUiState.Success
            assertEquals(2000, state.albums.first().year)
            assertEquals(2021, state.albums.last().year)
        }
}
