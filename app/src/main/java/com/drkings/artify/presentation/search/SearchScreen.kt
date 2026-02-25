package com.drkings.artify.presentation.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drkings.artify.R
import com.drkings.artify.domain.entity.ArtistEntity
import com.drkings.artify.ui.theme.Green40
import com.drkings.artify.ui.theme.Green60
import com.drkings.artify.ui.theme.Neutral15
import com.drkings.artify.ui.theme.Neutral20
import com.drkings.artify.ui.theme.Neutral6
import com.drkings.artify.ui.theme.NeutralVariant20
import com.drkings.artify.ui.theme.NeutralVariant40
import com.drkings.artify.ui.theme.NeutralVariant60
import com.drkings.artify.ui.theme.NeutralVariant90

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navigateToDetails: (String) -> Unit,
    searchViewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by searchViewModel.uiState.collectAsStateWithLifecycle()
    val query by searchViewModel.query.collectAsStateWithLifecycle()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Neutral6)
        ) {
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 22.sp,
                color = NeutralVariant90,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, start = 20.dp, bottom = 16.dp)
            )

            SearchField(
                query = query,
                onQueryChange = searchViewModel::onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            )

            // AnimatedContent transiciona suavemente entre estados del sealed interface
            AnimatedContent(
                targetState = uiState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "SearchStateTransition"
            ) { state ->
                when (state) {
                    is SearchUiState.Empty -> EmptyStateContent()
                    is SearchUiState.Loading -> LoadingContent()
                    is SearchUiState.Error -> ErrorContent(
                        message = state.message,
                        onRetry = { }//viewModel::retry
                    )

                    is SearchUiState.Success -> SearchResultsList(
                        results = state.artist,
                        isLoadingNextPage = state.isLoadingNextPage,
                        onArtistClick = navigateToDetails,
                        onLoadMore = searchViewModel::loadNextPage
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isActive = query.isNotBlank()
    val bgColor = if (isActive) Neutral20 else Neutral15
    val borderColor = if (isActive) Green40 else NeutralVariant20
    val iconColor = if (isActive) Green60 else NeutralVariant40

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = TextStyle(color = NeutralVariant90, fontSize = 15.sp),
        cursorBrush = SolidColor(Green60),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp)),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.search_screen_search_content_desc),
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isBlank()) {
                        Text(
                            text = stringResource(R.string.search_screen_hint),
                            color = NeutralVariant40,
                            fontSize = 15.sp
                        )
                    }
                    innerTextField()
                }
                if (isActive) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.search_screen_clear_content_desc),
                            tint = NeutralVariant40,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun EmptyStateContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Neutral15)
                .border(1.dp, NeutralVariant20, RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Ícono musical con opacidad reducida para estado vacío
            Icon(
                painter = painterResource(R.drawable.ic_music_note),
                contentDescription = null,
                tint = Green60.copy(alpha = 0.7f),
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.search_screen_empty_title),
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = NeutralVariant90
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.search_screen_empty_subtitle),
            fontSize = 14.sp,
            color = NeutralVariant60,
            lineHeight = 22.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun LoadingContent() {
    Column(modifier = Modifier.fillMaxWidth()) {
        repeat(3) { SkeletonItem() }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = NeutralVariant40,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(text = message, color = NeutralVariant60, fontSize = 14.sp)
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Green60),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = stringResource(R.string.search_screen_error_retry),
                color = Neutral6,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SearchResultsList(
    results: List<ArtistEntity>,
    isLoadingNextPage: Boolean,
    onArtistClick: (String) -> Unit,
    onLoadMore: () -> Unit
) {
    val listState = rememberLazyListState()
    val isScrolledToEnd by remember { derivedStateOf { listState.isScrolledToEnd() } }

    // Dispara paginación cuando el usuario llega al final
    LaunchedEffect(isScrolledToEnd) {
        if (isScrolledToEnd) onLoadMore()
    }

    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
        items(items = results, key = { it.id }) { artist ->
            ArtistResultItem(
                artist = artist,
                onClick = { onArtistClick(artist.id) }
            )
        }
        if (isLoadingNextPage) {
            items(2) { SkeletonItem() }
        }
    }
}

// Extension para detectar scroll al final de la lista
private fun LazyListState.isScrolledToEnd(): Boolean {
    val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return false
    return lastVisible >= layoutInfo.totalItemsCount - 1
}