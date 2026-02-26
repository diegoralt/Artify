package com.drkings.artify.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.drkings.artify.domain.entity.AlbumEntity
import com.drkings.artify.domain.usecase.AlbumsDetailUseCase
import com.drkings.artify.presentation.core.AlbumsDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.flatMap
import kotlin.collections.map
import kotlin.collections.mapNotNull

@HiltViewModel
class AlbumsDetailViewModel @Inject constructor(
    private val albumsDetailUseCase: AlbumsDetailUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val artistId: Int = savedStateHandle.toRoute<AlbumsDetail>().artistId

    private var currentPage = FIRST_PAGE
    private var totalPages = Int.MAX_VALUE
    private val hasReachedEnd get() = currentPage >= totalPages

    private val _baseState = MutableStateFlow<AlbumsBaseState>(AlbumsBaseState.Loading)

    private val _filterState = MutableStateFlow(FilterState())

    private val _sortOrder = MutableStateFlow(SortOrder.NEWEST_FIRST)

    // ── UI State derivado: combina base + filtros + sort en un único StateFlow ─
    // combine re-ejecuta el bloque cada vez que cualquiera de los 3 flows emite,
    // manteniendo la UI siempre en sincronía sin lógica imperativa adicional
    val uiState: StateFlow<AlbumsUiState> = combine(
        _baseState,
        _filterState,
        _sortOrder
    ) { base, filters, sort ->
        when (base) {
            is AlbumsBaseState.Loading -> AlbumsUiState.Loading
            is AlbumsBaseState.Error -> AlbumsUiState.Error(base.message)
            is AlbumsBaseState.Success -> {
                val filtered = applyFilters(base.allAlbums, filters, sort)
                AlbumsUiState.Success(
                    albums = filtered,
                    isLoadingNextPage = base.isLoadingNextPage,
                    filterState = filters,
                    sortOrder = sort,
                    // Opciones disponibles calculadas sobre el total sin filtrar
                    availableYears = base.allAlbums.mapNotNull { it.year }.distinct().sorted(),
                    availableGenres = base.allAlbums.flatMap { it.genres }.distinct().sorted(),
                    availableLabels = base.allAlbums.map { it.label }.filter { it.isNotBlank() }
                        .distinct().sorted()
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AlbumsUiState.Loading
    )

    init {
        loadReleases()
    }

    fun retry() {
        resetPaginationState()
        loadReleases()
    }

    fun loadNextPage() {
        if (hasReachedEnd) return

        val current = _baseState.value as? AlbumsBaseState.Success ?: return
        if (current.isLoadingNextPage) return

        viewModelScope.launch {
            _baseState.update { (it as AlbumsBaseState.Success).copy(isLoadingNextPage = true) }
            currentPage++

            albumsDetailUseCase(
                artistId = artistId,
                page = currentPage,
                perPage = PAGE_SIZE
            )
                .onSuccess { result ->
                    totalPages = result.pagination.pages
                    _baseState.update {
                        val success = it as AlbumsBaseState.Success
                        success.copy(
                            allAlbums = success.allAlbums + result.albums,
                            isLoadingNextPage = false
                        )
                    }
                }
                .onFailure {
                    currentPage-- // Revierte para reintentar la misma página
                    _baseState.update { (it as AlbumsBaseState.Success).copy(isLoadingNextPage = false) }
                }
        }
    }

    fun toggleYear(year: Int) {
        _filterState.update { current ->
            val updated = current.years.toMutableSet()
                .apply { if (!add(year)) remove(year) }
            current.copy(years = updated)
        }
    }

    fun toggleGenre(genre: String) {
        _filterState.update { current ->
            val updated = current.genres.toMutableSet()
                .apply { if (!add(genre)) remove(genre) }
            current.copy(genres = updated)
        }
    }

    fun toggleLabel(label: String) {
        _filterState.update { current ->
            val updated = current.labels.toMutableSet()
                .apply { if (!add(label)) remove(label) }
            current.copy(labels = updated)
        }
    }

    fun clearFilters() {
        _filterState.value = FilterState()
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    private fun loadReleases() {
        viewModelScope.launch {
            _baseState.value = AlbumsBaseState.Loading

            albumsDetailUseCase(
                artistId = artistId,
                page = FIRST_PAGE,
                perPage = PAGE_SIZE
            )
                .onSuccess { result ->
                    totalPages = result.pagination.pages
                    _baseState.value = AlbumsBaseState.Success(
                        allAlbums = result.albums,
                        isLoadingNextPage = false
                    )
                }
                .onFailure { error ->
                    _baseState.value = AlbumsBaseState.Error(
                        message = error.message ?: "Unable to load albums"
                    )
                }
        }
    }

    private fun applyFilters(
        albums: List<AlbumEntity>,
        filters: FilterState,
        sort: SortOrder
    ): List<AlbumEntity> {
        return albums
            .filter { album ->
                // Set vacío = no hay filtro activo para esa dimensión
                val yearMatch = filters.years.isEmpty() || album.year in filters.years
                val genreMatch =
                    filters.genres.isEmpty() || album.genres.any { it in filters.genres }
                val labelMatch = filters.labels.isEmpty() || album.label in filters.labels
                yearMatch && genreMatch && labelMatch
            }
            .let { filtered ->
                when (sort) {
                    SortOrder.NEWEST_FIRST -> filtered.sortedByDescending { it.year ?: 0 }
                    SortOrder.OLDEST_FIRST -> filtered.sortedBy { it.year ?: Int.MAX_VALUE }
                }
            }
    }

    private fun resetPaginationState() {
        currentPage = FIRST_PAGE
        totalPages = Int.MAX_VALUE
    }

    private companion object {
        const val PAGE_SIZE = 30
        const val FIRST_PAGE = 1
    }
}

enum class SortOrder {
    NEWEST_FIRST,
    OLDEST_FIRST
}

data class FilterState(
    val years: Set<Int> = emptySet(),
    val genres: Set<String> = emptySet(),
    val labels: Set<String> = emptySet()
) {
    val isActive: Boolean get() = years.isNotEmpty() || genres.isNotEmpty() || labels.isNotEmpty()
}

// ── Base state (albums acumulados sin filtrar) ─────────────────────────────────
// Separado del UiState para que los filtros no borren los datos ya cargados

private sealed interface AlbumsBaseState {
    object Loading : AlbumsBaseState
    data class Error(val message: String) : AlbumsBaseState
    data class Success(
        val allAlbums: List<AlbumEntity>,
        val isLoadingNextPage: Boolean
    ) : AlbumsBaseState
}

// ── UI State (derivado = albums filtrados + metadata para la UI) ───────────────

sealed interface AlbumsUiState {
    object Loading : AlbumsUiState
    data class Error(val message: String) : AlbumsUiState
    data class Success(
        val albums: List<AlbumEntity>,          // Lista ya filtrada y ordenada
        val isLoadingNextPage: Boolean,
        val filterState: FilterState,
        val sortOrder: SortOrder,
        val availableYears: List<Int>,          // Opciones para el filtro de año
        val availableGenres: List<String>,      // Opciones para el filtro de género
        val availableLabels: List<String>       // Opciones para el filtro de etiqueta
    ) : AlbumsUiState
}