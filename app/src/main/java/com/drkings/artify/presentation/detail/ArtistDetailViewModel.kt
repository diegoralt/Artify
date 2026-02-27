package com.drkings.artify.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.drkings.artify.domain.entity.ArtistDetailEntity
import com.drkings.artify.domain.usecase.ArtistDetailUseCase
import com.drkings.artify.presentation.core.ArtistDetail
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

    private val artistId: Int = savedStateHandle.toRoute<ArtistDetail>().artistId

    init {
        loadArtistDetail()
    }

    fun retry() {
        loadArtistDetail()
    }

    private fun loadArtistDetail() {
        viewModelScope.launch {
            _uiState.value = ArtistDetailUiState.Loading

            artistDetailUseCase(artistId)
                .onSuccess { artist ->
                    _uiState.value = ArtistDetailUiState.Success(artist)
                }
                .onFailure {
                    _uiState.value = ArtistDetailUiState.Error
                }
        }
    }
}

sealed interface ArtistDetailUiState {
    object Loading : ArtistDetailUiState
    object Error : ArtistDetailUiState
    data class Success(val artist: ArtistDetailEntity) : ArtistDetailUiState
}