package com.drkings.artify.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drkings.artify.domain.entity.ArtistEntity
import com.drkings.artify.domain.usecase.SearchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val searchUseCase: SearchUseCase) : ViewModel() {
    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Empty(true))
    val uiState = _uiState.asStateFlow()

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    private var currentPage = FIRST_PAGE
    private var totalPages = Int.MAX_VALUE
    private var currentQuery = ""
    private val hasReachedEnd get() = currentPage >= totalPages

    init {
        observeQueryWithDebounce()
    }

    @OptIn(FlowPreview::class)
    private fun observeQueryWithDebounce() {
        viewModelScope.launch {
            _query
                .debounce(DEBOUNCE_MS)
                .filter { it.isNotBlank() } // ignora queries vacíos
                .distinctUntilChanged() // ignora el mismo valor repetido
                .collectLatest { query ->
                    val current = _uiState.value
                    if (current is SearchUiState.Success && current.isLoadingNextPage) {
                        _uiState.value = current.copy(isLoadingNextPage = false)
                    }
                    resetPaginationState()
                    performSearch(query = query)
                }
        }
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        currentQuery = newQuery

        if (newQuery.isBlank()) {
            _uiState.value = SearchUiState.Empty(isBeforeQuery = true)
        }
    }

    fun loadNextPage() {
        if (hasReachedEnd) return

        val current = _uiState.value as? SearchUiState.Success ?: return

        // Guard: si ya está cargando la siguiente página, descartar la llamada
        if (current.isLoadingNextPage) return

        viewModelScope.launch {
            // Notifica a la UI para mostrar los skeletons al final de la lista
            _uiState.value = current.copy(isLoadingNextPage = true)
            currentPage++

            searchUseCase(
                query = currentQuery,
                page = currentPage,
                perPage = PAGE_SIZE
            )
                .onSuccess { result ->
                    // Si Discogs devuelve menos items que PAGE_SIZE, no hay más páginas
                    totalPages = result.pagination.pages

                    _uiState.value = current.copy(
                        // Acumulación: los nuevos resultados se agregan a los existentes
                        artists = current.artists + result.artists,
                        isLoadingNextPage = false
                    )
                }
                .onFailure {
                    // Revertir el contador para que el próximo intento pida
                    // la misma página fallida, no la siguiente
                    currentPage--
                    _uiState.value = current.copy(isLoadingNextPage = false)
                }
        }
    }

    fun retry() {
        if (currentQuery.isBlank()) return
        resetPaginationState()
        performSearch(query = currentQuery)
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading

            searchUseCase(
                query = query,
                page = FIRST_PAGE,
                perPage = PAGE_SIZE
            )
                .onSuccess { result ->
                    totalPages = result.pagination.pages

                    _uiState.value = if (result.artists.isEmpty()) {
                        // Discogs respondió OK pero sin resultados para este query
                        SearchUiState.Empty(isBeforeQuery = false)
                    } else {
                        SearchUiState.Success(artists = result.artists, isLoadingNextPage = false)
                    }
                }
                .onFailure {
                    _uiState.value = SearchUiState.Error
                }
        }
    }

    private fun resetPaginationState() {
        currentPage = FIRST_PAGE
        totalPages = Int.MAX_VALUE
    }

    private companion object {
        const val DEBOUNCE_MS = 400L // ms de espera antes de disparar búsqueda
        const val PAGE_SIZE = 30 // resultados por página
        const val FIRST_PAGE = 1
    }
}

sealed interface SearchUiState {
    data class Empty(val isBeforeQuery: Boolean) : SearchUiState
    object Loading : SearchUiState
    object Error : SearchUiState
    data class Success(
        val artists: List<ArtistEntity>,
        val isLoadingNextPage: Boolean
    ) : SearchUiState
}
