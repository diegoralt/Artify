package com.drkings.artify.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drkings.artify.domain.entity.ArtistDetailEntity
import com.drkings.artify.domain.usecase.ArtistDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    private val artistDetailUseCase: ArtistDetailUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow<ArtistDetailUiState>(ArtistDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val artistId: Int = checkNotNull(savedStateHandle[NAV_ARG_ARTIST_ID]) {
        "artistId is required in navigation route: artist_detail/{$NAV_ARG_ARTIST_ID}"
    }

    init {
        loadArtistDetail()
    }

    fun loadArtistDetail() {
        viewModelScope.launch {
            _uiState.value = ArtistDetailUiState.Loading

            artistDetailUseCase(artistId)
                .onSuccess { artist ->
                    _uiState.value = ArtistDetailUiState.Success(artist)
                }
                .onFailure { error ->
                    _uiState.value = ArtistDetailUiState.Error(
                        message = error.message ?: "Unable to load artist details"
                    )
                }
        }
    }

    companion object {
        const val NAV_ARG_ARTIST_ID = "artistId"
    }
}

sealed interface ArtistDetailUiState {
    object Loading : ArtistDetailUiState
    data class Error(val message: String) : ArtistDetailUiState
    data class Success(val artist: ArtistDetailEntity) : ArtistDetailUiState
}