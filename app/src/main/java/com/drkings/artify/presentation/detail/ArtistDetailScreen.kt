package com.drkings.artify.presentation.detail

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Dimension
import coil3.size.Scale
import coil3.size.Size
import com.drkings.artify.R
import com.drkings.artify.domain.entity.ArtistDetailEntity
import com.drkings.artify.domain.entity.MemberEntity
import com.drkings.artify.presentation.core.ErrorContent
import com.drkings.artify.ui.theme.Green60
import com.drkings.artify.ui.theme.Neutral15
import com.drkings.artify.ui.theme.Neutral6
import com.drkings.artify.ui.theme.NeutralVariant20
import com.drkings.artify.ui.theme.NeutralVariant40
import com.drkings.artify.ui.theme.NeutralVariant60
import com.drkings.artify.ui.theme.NeutralVariant90

private const val BIO_COLLAPSED_LINES = 4

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    artistDetailViewModel: ArtistDetailViewModel = hiltViewModel(),
    navigateToBack: () -> Unit,
    navigateToAlbums: (artistId: Int) -> Unit
) {
    val uiState by artistDetailViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Neutral6)
        ) {
            when (val state = uiState) {
                is ArtistDetailUiState.Loading -> ArtistDetailLoadingContent()
                is ArtistDetailUiState.Error -> ErrorContent(
                    artistDetailViewModel::retry
                )

                is ArtistDetailUiState.Success -> ArtistDetailContent(
                    artist = state.artist,
                    onDiscographyClick = { navigateToAlbums(state.artist.id) }
                )
            }
        }
    }
}

@Composable
private fun ArtistDetailContent(
    artist: ArtistDetailEntity,
    onDiscographyClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    val heroImageUrl by remember { mutableStateOf(artist.image) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Neutral6)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {

            HeroImageSection(
                imageUrl = heroImageUrl,
                artistName = artist.name
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                ArtistNameSection(name = artist.name)

                if (artist.profile.isNotBlank()) {
                    BiographySection(profile = artist.profile)
                }

                if (artist.members.isNotEmpty()) {
                    MembersSection(members = artist.members)
                }

                DiscographyButton(onClick = onDiscographyClick)
            }
        }
    }
}

@Composable
private fun HeroImageSection(
    imageUrl: String?,
    artistName: String
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
                .drawBehind {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF0D2019), Color(0xFF1A3A28)),
                            start = Offset(0f, 0f),
                            end = Offset(
                                size.width,
                                size.height
                            )  // ← coordenadas finitas garantizadas
                        )
                    )
                }
        )

        if (imageUrl != null) {
            AsyncImage(
                model = imageRequest,
                contentDescription = stringResource(
                    R.string.artist_detail_screen_hero_content_desc,
                    artistName
                ),
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
    }
}

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

@Composable
private fun BiographySection(profile: String) {
    var expanded by remember { mutableStateOf(false) }

    // Limpia etiquetas BBCode de Discogs ([b], [u], \r\n, etc.)
    val cleanProfile = remember(profile) {
        profile
            .replace(Regex("\\[/?[biu]]"), "")
            .replace(Regex("\\[/?[a-z]+]"), "")
            .replace("\\r\\n", "\n")
            .replace("\\n", "\n")
            .trim()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionLabel(text = stringResource(R.string.artist_detail_screen_biography))

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
                            if (expanded) R.string.artist_detail_screen_bio_less
                            else R.string.artist_detail_screen_bio_more
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

@Composable
private fun MembersSection(members: List<MemberEntity>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionLabel(text = stringResource(R.string.artist_detail_screen_members))

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
            .data(member.imageUrl)
            // Tamaño fijo para evitar bitmaps sobredimensionados en el LazyRow
            .size(Size(128, Dimension.Undefined))
            .scale(Scale.FIT)
            .crossfade(200)
            .memoryCacheKey("member_${member.id}")
            .diskCacheKey("member_${member.id}")
            .build()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.width(72.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                // Fondo siempre presente como placeholder
                .background(Neutral15)
                .then(
                    Modifier.border(1.dp, NeutralVariant20, CircleShape)
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            if (member.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = member.name,
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.TopCenter,
                    error = ColorPainter(Color.Transparent),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = NeutralVariant40,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Text(
            text = member.name,
            fontSize = 11.sp,
            color = NeutralVariant90,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

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
                text = stringResource(R.string.artist_detail_screen_discography_button),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Neutral6
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Neutral6,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ArtistDetailLoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Neutral6),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Green60)
    }
}

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

@Preview(showBackground = true, backgroundColor = 0xFF060E0B)
@Composable
private fun ArtistDetailContentPreview() {
    ArtistDetailContent(
        artist = ArtistDetailEntity(
            id = 29735,
            name = "Coldplay",
            profile = "Pop rock band from London, England.\r\nFormed 1997.\r\n\r\n[b][u]Line-up:[/u][/b]\r\nJonny Buckland - Guitar/Keys\\Backing vocals (1997-)\r\nWill Champion - Drums/Backing vocals (1998-)\r\nGuy Berryman - Bass/Keys (1997-)\r\nChris Martin - Vocals/Piano\\Acoustic guitar(1997-)\r\nPhil Harvey - Manager/Creative director (1998-2002, 2006-)",
            image = "https://i.discogs.com/sBor9_gG6g8eU12WLhcoDn_N88zv3F5VXh8i_lkVaW0/rs:fit/g:sm/q:90/h:385/w:600/czM6Ly9kaXNjb2dz/LWRhdGFiYXNlLWlt/YWdlcy9BLTI5NzM1/LTE3MjAwNjU1MTEt/NzM3Mi5qcGVn.jpeg",
            members = listOf(
                MemberEntity(42610, "Chris Martin", imageUrl = ""),
                MemberEntity(530745, "Guy Berryman", imageUrl = ""),
                MemberEntity(530746, "Will Champion", imageUrl = ""),
                MemberEntity(530747, "Jon Buckland", imageUrl = ""),
                MemberEntity(530749, "Phil Harvey", imageUrl = "")
            )
        ),
        onDiscographyClick = {}
    )
}