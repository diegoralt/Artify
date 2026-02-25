package com.drkings.artify.presentation.detail

import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

private const val BIO_COLLAPSED_LINES = 4

@Composable
fun ArtistDetailScreen(
    viewModel: ArtistDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onDiscographyClick: (artistId: Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is ArtistDetailUiState.Loading -> ArtistDetailLoadingContent(onBackClick)
        is ArtistDetailUiState.Error -> ArtistDetailErrorContent(state.message, onBackClick)
        is ArtistDetailUiState.Success -> ArtistDetailContent(
            artist = state.artist,
            onBackClick = onBackClick,
            onDiscographyClick = { onDiscographyClick(state.artist.id) }
        )
    }
}

// ── Contenido principal ───────────────────────────────────────────────────────

@Composable
private fun ArtistDetailContent(
    artist: ArtistDetailEntity,
    onBackClick: () -> Unit,
    onDiscographyClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Imagen primaria del artista — fallback a la primera disponible si no hay primary
    val heroImageUrl = remember(artist.images) {
        artist.images.firstOrNull { it.type == "primary" }?.resourceUrl
            ?: artist.images.firstOrNull()?.resourceUrl
    }

    Box(modifier = Modifier.fillMaxSize().background(Neutral6)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {

            // ── Hero Image ────────────────────────────────────────────────────
            HeroImageSection(
                imageUrl = heroImageUrl,
                artistName = artist.name,
                onBackClick = onBackClick
            )

            // ── Contenido con padding ─────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                // ── Nombre del artista ────────────────────────────────────────
                ArtistNameSection(name = artist.name)

                // ── Biografía ─────────────────────────────────────────────────
                if (artist.profile.isNotBlank()) {
                    BiographySection(profile = artist.profile)
                }

                // ── Miembros ──────────────────────────────────────────────────
                if (artist.members.isNotEmpty()) {
                    MembersSection(members = artist.members)
                }

                // ── Botón discografía ─────────────────────────────────────────
                DiscographyButton(onClick = onDiscographyClick)
            }
        }
    }
}

// ── Hero Image ────────────────────────────────────────────────────────────────

@Composable
private fun HeroImageSection(
    imageUrl: String?,
    artistName: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    val imageRequest = remember(imageUrl) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .scale(Scale.FILL)
            .crossfade(300)
            .build()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        // Fondo degradado siempre presente como placeholder
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF0D2019), Color(0xFF1A3A28)),
                        start = Offset(0f, 0f),
                        end = Offset(Float.MAX_VALUE, Float.MAX_VALUE)
                    )
                )
        )

        if (imageUrl != null) {
            AsyncImage(
                model = imageRequest,
                contentDescription = stringResource(R.string.artist_detail_hero_cd, artistName),
                contentScale = ContentScale.Crop,
                error = ColorPainter(Color.Transparent),
                modifier = Modifier.fillMaxSize()
            )
        }

        // Degradado inferior para transición suave hacia el fondo de la pantalla
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Neutral6)
                    )
                )
        )

        // Botón back superpuesto sobre la imagen
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 8.dp, top = 4.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Neutral6.copy(alpha = 0.6f))
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = stringResource(R.string.back_cd),
                tint = NeutralVariant90
            )
        }
    }
}

// ── Nombre del artista ────────────────────────────────────────────────────────

@Composable
private fun ArtistNameSection(name: String) {
    Text(
        text = name,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = NeutralVariant90,
        lineHeight = 34.sp,
        modifier = Modifier.padding(top = 4.dp)
    )
}

// ── Sección de Biografía ──────────────────────────────────────────────────────

@Composable
private fun BiographySection(profile: String) {
    var expanded by remember { mutableStateOf(false) }

    // Limpia etiquetas BBCode de Discogs ([b], [u], \r\n, etc.)
    val cleanProfile = remember(profile) {
        profile
            .replace(Regex("\\[/?[biu]\\]"), "")
            .replace(Regex("\\[/?[a-z]+\\]"), "")
            .replace("\\r\\n", "\n")
            .replace("\\n", "\n")
            .trim()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionLabel(text = stringResource(R.string.artist_detail_biography))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Neutral15)
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = cleanProfile,
                    fontSize = 13.sp,
                    color = NeutralVariant60,
                    lineHeight = 20.sp,
                    // animateContentSize anima la expansión sin necesidad de AnimatedVisibility
                    maxLines = if (expanded) Int.MAX_VALUE else BIO_COLLAPSED_LINES,
                    overflow = if (expanded) TextOverflow.Visible else TextOverflow.Ellipsis,
                    modifier = Modifier.animateContentSize()
                )

                // "Ver más / Ver menos" — solo visible si el texto es largo
                val arrowRotation by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = if (expanded) 180f else 0f,
                    label = "arrowRotation"
                )
                Row(
                    modifier = Modifier
                        .clickable { expanded = !expanded }
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(
                            if (expanded) R.string.artist_detail_bio_less
                            else R.string.artist_detail_bio_more
                        ),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Green60
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Green60,
                        modifier = Modifier
                            .size(16.dp)
                            .rotate(arrowRotation)
                    )
                }
            }
        }
    }
}

// ── Sección de Miembros ───────────────────────────────────────────────────────

@Composable
private fun MembersSection(members: List<MemberEntity>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionLabel(text = stringResource(R.string.artist_detail_members))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(items = members, key = { it.id }) { member ->
                MemberItem(member = member)
            }
        }
    }
}

@Composable
private fun MemberItem(member: MemberEntity) {
    val context = LocalContext.current

    val imageRequest = remember(member.id) {
        ImageRequest.Builder(context)
            .data(member.thumbnailUrl)
            // Tamaño fijo para evitar bitmaps sobredimensionados en el LazyRow
            .size(Size(56, 56))
            .scale(Scale.FILL)
            .crossfade(200)
            .memoryCacheKey("member_${member.id}")
            .diskCacheKey("member_${member.id}")
            .build()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.width(64.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                // Fondo siempre presente como placeholder
                .background(Neutral15)
                .then(
                    // Borde verde para miembros activos
                    if (member.active) Modifier.border(1.5.dp, Green40, CircleShape)
                    else Modifier.border(1.dp, NeutralVariant20, CircleShape)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (member.thumbnailUrl != null) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = member.name,
                    contentScale = ContentScale.Crop,
                    error = ColorPainter(Color.Transparent),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = NeutralVariant40,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Text(
            text = member.name,
            fontSize = 11.sp,
            color = if (member.active) NeutralVariant90 else NeutralVariant40,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

// ── Botón Discografía ─────────────────────────────────────────────────────────

@Composable
private fun DiscographyButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Green60)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.artist_detail_discography_btn),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Neutral6
            )
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Neutral6,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ── Loading ───────────────────────────────────────────────────────────────────

@Composable
private fun ArtistDetailLoadingContent(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Neutral6),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Green60)
    }
}

// ── Error ─────────────────────────────────────────────────────────────────────

@Composable
private fun ArtistDetailErrorContent(message: String, onBackClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Neutral6),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Text(text = message, color = NeutralVariant60, fontSize = 14.sp)
            OutlinedButton(
                onClick = onBackClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Green60),
                border = androidx.compose.foundation.BorderStroke(1.dp, Green40)
            ) {
                Text(stringResource(R.string.back_cd))
            }
        }
    }
}

// ── Componente reutilizable ───────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = NeutralVariant40,
        letterSpacing = 1.5.sp
    )
}

// ── Sealed UI State ───────────────────────────────────────────────────────────

sealed interface ArtistDetailUiState {
    object Loading : ArtistDetailUiState
    data class Error(val message: String) : ArtistDetailUiState
    data class Success(val artist: ArtistDetailEntity) : ArtistDetailUiState
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF060E0B)
@Composable
private fun ArtistDetailContentPreview() {
    ArtistDetailContent(
        artist = ArtistDetailEntity(
            id = 29735,
            name = "Coldplay",
            profile = "Pop rock band from London, England.\nFormed 1997.\n\nJonny Buckland - Guitar\nWill Champion - Drums\nGuy Berryman - Bass\nChris Martin - Vocals",
            images = emptyList(),
            members = listOf(
                MemberEntity(42610, "Chris Martin", active = true, thumbnailUrl = null),
                MemberEntity(530745, "Guy Berryman", active = true, thumbnailUrl = null),
                MemberEntity(530746, "Will Champion", active = true, thumbnailUrl = null),
                MemberEntity(530747, "Jon Buckland", active = true, thumbnailUrl = null),
                MemberEntity(530749, "Phil Harvey", active = false, thumbnailUrl = null)
            )
        ),
        onBackClick = {},
        onDiscographyClick = {}
    )
}