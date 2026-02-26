package com.drkings.artify.presentation.detail

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Scale
import coil3.size.Size
import com.drkings.artify.R
import com.drkings.artify.domain.entity.AlbumEntity
import com.drkings.artify.presentation.core.ErrorContent
import com.drkings.artify.ui.theme.Green20
import com.drkings.artify.ui.theme.Green40
import com.drkings.artify.ui.theme.Green60
import com.drkings.artify.ui.theme.Neutral15
import com.drkings.artify.ui.theme.Neutral20
import com.drkings.artify.ui.theme.Neutral6
import com.drkings.artify.ui.theme.NeutralVariant20
import com.drkings.artify.ui.theme.NeutralVariant40
import com.drkings.artify.ui.theme.NeutralVariant60
import com.drkings.artify.ui.theme.NeutralVariant90
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumsDetailScreen(
    viewModel: AlbumsDetailViewModel = hiltViewModel(),
    navigateToBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val artistName by remember { mutableStateOf(viewModel.artistName) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = artistName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NeutralVariant90,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = navigateToBack,
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(start = 8.dp, top = 4.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Neutral6.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back_button_content_desc),
                            tint = NeutralVariant90
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Neutral6)
            )
        },
        modifier = Modifier.fillMaxSize(),
        containerColor = Neutral6,
        contentColor = NeutralVariant90
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is AlbumsUiState.Loading -> AlbumsLoadingContent()
                is AlbumsUiState.Error -> ErrorContent(
                    onRetry = viewModel::retry
                )

                is AlbumsUiState.Success -> AlbumsSuccessContent(
                    state = state,
                    onLoadMore = viewModel::loadNextPage,
                    onToggleYear = viewModel::toggleYear,
                    onToggleGenre = viewModel::toggleGenre,
                    onToggleLabel = viewModel::toggleLabel,
                    onClearFilters = viewModel::clearFilters,
                    onSortChange = viewModel::setSortOrder
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlbumsSuccessContent(
    state: AlbumsUiState.Success,
    onLoadMore: () -> Unit,
    onToggleYear: (Int) -> Unit,
    onToggleGenre: (String) -> Unit,
    onToggleLabel: (String) -> Unit,
    onClearFilters: () -> Unit,
    onSortChange: (SortOrder) -> Unit
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var activeSheet by remember { mutableStateOf<ActiveSheet?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(activeSheet) {
        if (activeSheet == null) return@LaunchedEffect
        delay(50)
        sheetState.show()
    }

    // Dispara loadNextPage cuando el usuario está a 3 items del final
    val shouldLoadMore by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = info.totalItemsCount
            total > 0 && lastVisible >= total - 5 && !state.isLoadingNextPage
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }

    val albumsKey = state.albums.firstOrNull()?.id
    val sortKey = state.sortOrder
    val filterKey = state.filterState

    LaunchedEffect(albumsKey, sortKey, filterKey) {
        // Solo hace scroll si ya hay contenido visible y no estamos en la
        // posición 0 (evita scroll innecesario en la carga inicial).
        if (listState.firstVisibleItemIndex > 0) {
            listState.animateScrollToItem(0)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Neutral6)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Filter chip bar — SIEMPRE visible, igual al mockup ────────────
            FilterChipBar(
                filterState = state.filterState,
                onAllClick = onClearFilters,
                onYearClick = { activeSheet = ActiveSheet.YEAR },
                onGenreClick = { activeSheet = ActiveSheet.GENRE },
                onLabelClick = { activeSheet = ActiveSheet.LABEL }
            )

            // ── Sort indicator ────────────────────────────────────────────────
            SortIndicatorRow(
                sortOrder = state.sortOrder,
                onSortClick = { activeSheet = ActiveSheet.SORT })

            // ── List / empty ──────────────────────────────────────────────────
            if (state.albums.isEmpty()) {
                AlbumsEmptyFiltered(onClearFilters = onClearFilters)
            } else {
                AlbumsList(
                    albums = state.albums,
                    isLoadingNextPage = state.isLoadingNextPage,
                    listState = listState
                )
            }
        }

        // ── Bottom sheets ─────────────────────────────────────────────────────
        if (activeSheet != null) {
            ModalBottomSheet(
                onDismissRequest = { activeSheet = null },
                sheetState = sheetState,
                containerColor = Neutral15,
                contentColor = NeutralVariant90,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                contentWindowInsets = { WindowInsets(0) }
            ) {
                when (activeSheet) {
                    ActiveSheet.YEAR -> YearFilterSheet(
                        available = state.availableYears,
                        selected = state.filterState.years,
                        onToggle = onToggleYear,
                        onClear = { state.filterState.years.forEach { onToggleYear(it) } },
                        onApply = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) activeSheet = null
                            }
                        }
                    )

                    ActiveSheet.GENRE -> GenreFilterSheet(
                        available = state.availableGenres,
                        selected = state.filterState.genres,
                        onToggle = onToggleGenre,
                        onClear = { state.filterState.genres.forEach { onToggleGenre(it) } },
                        onApply = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) activeSheet = null
                            }
                        }
                    )

                    ActiveSheet.LABEL -> LabelFilterSheet(
                        available = state.availableLabels,
                        selected = state.filterState.labels,
                        onToggle = onToggleLabel,
                        onClear = { state.filterState.labels.forEach { onToggleLabel(it) } },
                        onApply = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) activeSheet = null
                            }
                        }
                    )

                    ActiveSheet.SORT -> SortPickerSheet(
                        current = state.sortOrder,
                        onSelect = { order ->
                            onSortChange(order)
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) activeSheet = null
                            }
                        }
                    )

                    null -> Unit
                }
            }
        }
    }
}

@Composable
private fun SortIndicatorRow(sortOrder: SortOrder, onSortClick: () -> Unit) {
    val label = when (sortOrder) {
        SortOrder.NEWEST_FIRST -> stringResource(R.string.albums_detail_screen_sort_newest)
        SortOrder.OLDEST_FIRST -> stringResource(R.string.albums_detail_screen_sort_oldest)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${stringResource(R.string.albums_detail_screen_sort_prefix)} $label",
            fontSize = 11.sp,
            color = Green60,
            fontWeight = FontWeight.SemiBold
        )

        IconButton(onClick = onSortClick, modifier = Modifier.size(24.dp)) {
            Icon(
                imageVector = Icons.Default.SwapVert,
                contentDescription = stringResource(R.string.albums_detail_screen_sort_content_desc),
                tint = NeutralVariant60
            )
        }
    }
}

@Composable
private fun FilterChipBar(
    filterState: FilterState,
    onAllClick: () -> Unit,
    onYearClick: () -> Unit,
    onGenreClick: () -> Unit,
    onLabelClick: () -> Unit
) {
    val allActive = !filterState.isActive

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── All ───────────────────────────────────────────────────────────────
        FilterBarChip(
            label = stringResource(R.string.albums_detail_screen_filter_all),
            active = allActive,
            hasArrow = false,
            onClick = onAllClick
        )

        // ── Year ──────────────────────────────────────────────────────────────
        FilterBarChip(
            label = stringResource(R.string.albums_detail_screen_filter_year),
            active = filterState.years.isNotEmpty(),
            hasArrow = true,
            onClick = onYearClick
        )

        // ── Genre ─────────────────────────────────────────────────────────────
        FilterBarChip(
            label = stringResource(R.string.albums_detail_screen_filter_genre),
            active = filterState.genres.isNotEmpty(),
            hasArrow = true,
            onClick = onGenreClick
        )

        // ── Label ─────────────────────────────────────────────────────────────
        FilterBarChip(
            label = stringResource(R.string.albums_detail_screen_filter_label),
            active = filterState.labels.isNotEmpty(),
            hasArrow = true,
            onClick = onLabelClick
        )
    }
}

@Composable
private fun FilterBarChip(
    label: String,
    active: Boolean,
    hasArrow: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (active) Green60 else Neutral15
    val borderColor = if (active) Green60 else NeutralVariant20
    val textColor = if (active) Neutral6 else NeutralVariant60

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
        if (hasArrow) {
            Text(
                text = "↓",
                fontSize = 11.sp,
                color = textColor
            )
        }
    }
}

@Composable
private fun AlbumsList(
    albums: List<AlbumEntity>,
    isLoadingNextPage: Boolean,
    listState: LazyListState
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        items(
            items = albums,
            key = { it.id }
        ) { album ->
            AlbumRow(album = album)
        }

        // Skeleton de carga al final para la siguiente página
        if (isLoadingNextPage) {
            items(count = 4, key = { "skeleton_$it" }) {
                AlbumRowSkeleton()
            }
        }
    }
}

@Composable
private fun AlbumRow(album: AlbumEntity) {
    val context = LocalContext.current

    // ImageRequest con clave por ID para que el cache sobreviva cambios de URL
    val imageRequest = remember(album.id) {
        ImageRequest.Builder(context)
            .data(album.thumbUrl.ifBlank { null })
            .size(Size(120, 120))
            .scale(Scale.FILL)
            .crossfade(200)
            .memoryCacheKey("album_cover_${album.id}")
            .diskCacheKey("album_cover_${album.id}")
            .build()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .bottomDivider(NeutralVariant20)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── Cover art ─────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Neutral15),
            contentAlignment = Alignment.Center
        ) {
            if (album.thumbUrl.isNotBlank()) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    error = ColorPainter(Color.Transparent),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = NeutralVariant40,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // ── Metadata ──────────────────────────────────────────────────────────
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Título
            Text(
                text = album.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = NeutralVariant90,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Año — en verde, como en el mockup
            album.year?.let { year ->
                Text(
                    text = year.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Green60
                )
            }

            // Géneros como texto plano separado por · (ej. "Rock · Pop")
            if (album.genres.isNotEmpty()) {
                Text(
                    text = album.genres.take(2).joinToString(" · "),
                    fontSize = 12.sp,
                    color = NeutralVariant60,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Sello con ícono shield — igual al mockup
            if (album.label.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = NeutralVariant40,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = album.label,
                        fontSize = 11.sp,
                        color = NeutralVariant40,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun AlbumRowSkeleton() {
    val shimmer = rememberShimmerBrush()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .bottomDivider(NeutralVariant20)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(shimmer)
        )
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.58f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmer)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmer)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.28f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmer)
            )
        }
    }
}

@Composable
private fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val offset by transition.animateFloat(
        initialValue = -600f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )
    return Brush.linearGradient(
        colors = listOf(Neutral15, Neutral20, Neutral15),
        start = Offset(offset, 0f),
        end = Offset(offset + 400f, 0f)
    )
}

@Composable
private fun AlbumsEmptyFiltered(onClearFilters: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = null,
                tint = NeutralVariant40,
                modifier = Modifier.size(52.dp)
            )
            Text(
                text = stringResource(R.string.albums_detail_screen_empty_filtered_title),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = NeutralVariant90,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.albums_detail_screen_empty_filtered_subtitle),
                fontSize = 13.sp,
                color = NeutralVariant60,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp
            )
            OutlinedButton(
                onClick = onClearFilters,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Green60),
                border = BorderStroke(1.dp, Green40)
            ) {
                Text(
                    text = stringResource(R.string.albums_detail_screen_filter_clear_all),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun YearFilterSheet(
    available: List<Int>,
    selected: Set<Int>,
    onToggle: (Int) -> Unit,
    onClear: () -> Unit,
    onApply: () -> Unit
) {
    // Años agrupados por década para no saturar la UI
    val decades = remember(available) {
        available
            .groupBy { (it / 10) * 10 }
            .entries
            .sortedByDescending { it.key }
    }

    FilterSheetScaffold(
        title = stringResource(R.string.albums_detail_screen_filter_year),
        hasActive = selected.isNotEmpty(),
        onClear = onClear,
        onApply = onApply
    ) {
        // items() vive en LazyListScope — cada década es un item lazy
        items(
            count = decades.size,
            key = { decades[it].key }
        ) { index ->
            val (decade, yearsInDecade) = decades[index]
            FilterOptionRow(
                label = "${decade}s",
                checked = yearsInDecade.all { it in selected },
                onClick = { yearsInDecade.forEach { onToggle(it) } }
            )
        }
    }
}

@Composable
private fun GenreFilterSheet(
    available: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    onClear: () -> Unit,
    onApply: () -> Unit
) {
    FilterSheetScaffold(
        title = stringResource(R.string.albums_detail_screen_filter_genre),
        hasActive = selected.isNotEmpty(),
        onClear = onClear,
        onApply = onApply
    ) {
        items(
            count = available.size,
            key = { available[it] }
        ) { index ->
            val genre = available[index]
            FilterOptionRow(
                label = genre,
                checked = genre in selected,
                onClick = { onToggle(genre) }
            )
        }
    }
}

@Composable
private fun LabelFilterSheet(
    available: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    onClear: () -> Unit,
    onApply: () -> Unit
) {
    FilterSheetScaffold(
        title = stringResource(R.string.albums_detail_screen_filter_label),
        hasActive = selected.isNotEmpty(),
        onClear = onClear,
        onApply = onApply
    ) {
        items(
            count = available.size,
            key = { available[it] }
        ) { index ->
            val label = available[index]
            FilterOptionRow(
                label = label,
                checked = label in selected,
                onClick = { onToggle(label) }
            )
        }
    }
}

@Composable
private fun FilterSheetScaffold(
    title: String,
    hasActive: Boolean,
    onClear: () -> Unit,
    onApply: () -> Unit,
    content: LazyListScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .navigationBarsPadding()
    ) {
        // Encabezado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 4.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = NeutralVariant90
            )
            if (hasActive) {
                TextButton(onClick = onClear) {
                    Text(
                        text = stringResource(R.string.albums_detail_screen_filter_clear_all),
                        color = Green60,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        val lazyListState = rememberLazyListState()
        val nestedScrollConnection = remember(lazyListState) {
            object : NestedScrollConnection {
                override fun onPreScroll(
                    available: Offset,
                    source: NestedScrollSource
                ): Offset {
                    val atTop = !lazyListState.canScrollBackward
                    return if (available.y > 0 && atTop) Offset.Zero
                    else Offset.Zero  // en todos los demás casos, tampoco consumimos (la lista lo maneja)
                }
            }
        }

        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .nestedScroll(nestedScrollConnection),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            content = content
        )

        // Apply
        Button(
            onClick = onApply,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 24.dp)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Green60)
        ) {
            Text(
                text = stringResource(R.string.albums_detail_screen_filter_apply),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Neutral6
            )
        }
    }
}

@Composable
private fun FilterOptionRow(
    label: String,
    checked: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = NeutralVariant90
        )
        // Checkbox visual
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (checked) Green60 else Neutral20)
                .border(
                    width = if (checked) 0.dp else 1.dp,
                    color = if (checked) Color.Transparent else NeutralVariant40,
                    shape = RoundedCornerShape(6.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Neutral6,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun SortPickerSheet(
    current: SortOrder,
    onSelect: (SortOrder) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(R.string.albums_detail_screen_sort_title),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = NeutralVariant90,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        SortOrder.entries.forEach { order ->
            val label = when (order) {
                SortOrder.NEWEST_FIRST -> stringResource(R.string.albums_detail_screen_sort_newest)
                SortOrder.OLDEST_FIRST -> stringResource(R.string.albums_detail_screen_sort_oldest)
            }
            val isSelected = current == order

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) Green20 else Color.Transparent)
                    .clickable { onSelect(order) }
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    color = if (isSelected) Green60 else NeutralVariant90,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Green60,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AlbumsLoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Neutral6),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Green60,
            strokeWidth = 2.dp
        )
    }
}

private fun Modifier.bottomDivider(color: Color): Modifier = this.drawWithContent {
    drawContent()
    drawLine(
        color = color,
        start = Offset(0f, size.height),
        end = Offset(size.width, size.height),
        strokeWidth = 1.dp.toPx()
    )
}

private enum class ActiveSheet { YEAR, GENRE, LABEL, SORT }

@Preview(showBackground = true, backgroundColor = 0xFF060E0B)
@Composable
private fun AlbumsDetailScreenPreview() {
    AlbumsSuccessContent(
        state = AlbumsUiState.Success(
            albums = listOf(
                AlbumEntity(
                    1,
                    "A Head Full of Dreams",
                    "Coldplay",
                    2015,
                    "",
                    "Parlophone",
                    "",
                    listOf("Rock", "Pop")
                ),
                AlbumEntity(
                    2,
                    "Ghost Stories",
                    "Coldplay",
                    2014,
                    "",
                    "Parlophone",
                    "",
                    listOf("Pop")
                ),
                AlbumEntity(
                    3,
                    "Mylo Xyloto",
                    "Coldplay",
                    2011,
                    "",
                    "Parlophone",
                    "",
                    listOf("Rock")
                ),
                AlbumEntity(
                    4,
                    "Viva la Vida",
                    "Coldplay",
                    2008,
                    "",
                    "Parlophone",
                    "",
                    listOf("Alternative")
                )
            ),
            isLoadingNextPage = false,
            filterState = FilterState(),
            sortOrder = SortOrder.NEWEST_FIRST,
            availableYears = listOf(2008, 2011, 2014, 2015),
            availableGenres = listOf("Alternative", "Pop", "Rock"),
            availableLabels = listOf("Parlophone")
        ),
        onLoadMore = {},
        onToggleYear = {},
        onToggleGenre = {},
        onToggleLabel = {},
        onClearFilters = {},
        onSortChange = {}
    )
}