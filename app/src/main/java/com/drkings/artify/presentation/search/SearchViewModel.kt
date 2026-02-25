package com.drkings.artify.presentation.search

import androidx.lifecycle.ViewModel
import com.drkings.artify.presentation.model.Artist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SearchViewModel: ViewModel() {
    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Empty)
    val uiState = _uiState.asStateFlow()


}

sealed interface SearchUiState {
    object Empty : SearchUiState
    object Loading : SearchUiState
    data class Error(val message: String) : SearchUiState
    data class Success(
        val artist: List<Artist>,
        val isLoadingNextPage: Boolean,
        val onArtistClick: (String) -> Unit,
        val onLoadMore: () -> Unit
    ) : SearchUiState
}