package com.drkings.artify.presentation.search

import com.drkings.artify.domain.entity.ArtistEntity
import com.drkings.artify.domain.entity.PaginationEntity
import com.drkings.artify.domain.entity.SearchEntity
import com.drkings.artify.domain.usecase.SearchUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var searchUseCase: SearchUseCase
    private lateinit var viewModel: SearchViewModel

    private val fakeSearchEntity = SearchEntity(
        pagination = PaginationEntity(page = 1, pages = 3, items = 58),
        artists = listOf(
            ArtistEntity(
                id = 29735,
                uuid = "09e8638b-a98a-40da-8506-ae1c368492b9",
                name = "Coldplay",
                type = "artist",
                thumbUrl = "https://img.discogs.com/coldplay.jpg"
            ),
            ArtistEntity(
                id = 42610,
                uuid = "f380cc0f-ef08-492b-bf8a-84a8c0d7c9dc",
                name = "Chris Martin",
                type = "artist",
                thumbUrl = "https://img.discogs.com/chris.jpg"
            )
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        searchUseCase = mockk()
        viewModel = SearchViewModel(searchUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onQueryChange - blank query - uiState transitions to Empty when query is empty`() =
        runTest {
            // Given
            viewModel.onQueryChange("Coldplay")

            // When
            viewModel.onQueryChange("")

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is SearchUiState.Empty)
        }

    @Test
    fun `onQueryChange - non-blank query - uiState transitions to Success when query is not empty`() =
        runTest {
            // Given
            coEvery { searchUseCase(any(), any(), any()) } returns Result.success(fakeSearchEntity)

            // When
            viewModel.onQueryChange("Coldplay")
            advanceTimeBy(400L)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is SearchUiState.Success)
        }

    @Test
    fun `onQueryChange - non-blank query - uiState gives information when search is success`() =
        runTest {
            // Given
            coEvery { searchUseCase(any(), any(), any()) } returns Result.success(fakeSearchEntity)

            // When
            viewModel.onQueryChange("Coldplay")
            advanceTimeBy(400L)
            advanceUntilIdle()

            //Then
            val state = viewModel.uiState.value
            assertEquals(2, (state as SearchUiState.Success).artists.size)
            assertEquals("Coldplay", state.artists.first().name)
        }


    @Test
    fun `loadNextPage - black query - searchUseCase is never invoked when query is empty`() =
        runTest {
            // Given
            assertTrue(viewModel.uiState.value is SearchUiState.Empty)

            // When
            viewModel.loadNextPage()

            // Then
            coVerify(exactly = 0) { searchUseCase(any(), any(), any()) }
        }

    @Test
    fun `loadNextPage - query changes multiple times rapidly - searchUseCase is invoked only once due to debounce`() =
        runTest {
            // Given
            coEvery { searchUseCase(any(), any(), any()) } returns Result.success(fakeSearchEntity)

            // When
            viewModel.onQueryChange("Coldplay")
            viewModel.onQueryChange("Coldpl")
            viewModel.onQueryChange("Coldplay")
            viewModel.loadNextPage()
            advanceTimeBy(400L)
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { searchUseCase(any(), any(), any()) }
        }
}
