package com.drkings.artify.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Scale
import coil3.size.Size
import com.drkings.artify.domain.entity.ArtistEntity
import com.drkings.artify.ui.theme.Green60
import com.drkings.artify.ui.theme.NeutralVariant20
import com.drkings.artify.ui.theme.NeutralVariant40
import com.drkings.artify.ui.theme.NeutralVariant60
import com.drkings.artify.ui.theme.NeutralVariant90

private val AVATAR_SIZE = 52.dp

@Composable
fun ArtistResultItem(
    artist: ArtistEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp)
            .drawBottomBorder(color = NeutralVariant20),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ArtistAvatar(artist = artist)

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = artist.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = NeutralVariant90,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            ArtistTypeChip(artist.type)
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = NeutralVariant40,
            modifier = Modifier
                .size(18.dp)
                .alpha(0.4f)
        )
    }
}

@Composable
private fun ArtistAvatar(artist: ArtistEntity) {
    val avatarGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF1a3a2a), Color(0xFF2a5a3a)),
        start = Offset(0f, 0f), end = Offset(52f, 52f)
    )

    val context = LocalContext.current
    val imageRequest = remember(artist.thumbUrl) {
        ImageRequest.Builder(context)
            .data(artist.thumbUrl)
            .size(Size(AVATAR_SIZE.value.toInt(), AVATAR_SIZE.value.toInt()))
            .scale(Scale.FILL)
            .crossfade(200)
            .memoryCacheKey("avatar_${artist.id}")
            .diskCacheKey("avatar_${artist.id}")
            .build()
    }

    val fallbackIcon = Icons.Default.Person

    Box(
        modifier = Modifier
            .size(AVATAR_SIZE)
            .clip(CircleShape)
            .background(avatarGradient),
        contentAlignment = Alignment.Center
    ) {
        if (artist.thumbUrl.isNotEmpty()) {
            AsyncImage(
                model = imageRequest,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                error = ColorPainter(Color.Transparent),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        } else {
            Icon(
                imageVector = fallbackIcon,
                contentDescription = null,
                tint = NeutralVariant60,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun ArtistTypeChip(type: String) {
    val (bg, textColor, label) = Triple(
        Green60.copy(alpha = 0.12f),
        Green60,
        type.replaceFirstChar { it.uppercase() })
    Box(
        modifier = Modifier
            .height(20.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            letterSpacing = 0.5.sp
        )
    }
}

// Modifier extension para borde inferior sin afectar el layout
private fun Modifier.drawBottomBorder(color: Color): Modifier = this.then(
    Modifier.drawWithContent {
        drawContent()
        drawLine(
            color = color,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 1.dp.toPx()
        )
    }
)

@Preview(showBackground = true, backgroundColor = 0xFF060E0B)
@Composable
private fun ArtistResultItemSoloPreview() {
    ArtistResultItem(
        artist = ArtistEntity(
            id = 29735,
            name = "Coldplay",
            type = "artist",
            thumbUrl = "https://i.discogs.com/V90awgfHX4AGcdXYb6M4w8Sl-zzxrK0nsMET0pUXMTw/rs:fit/g:sm/q:40/h:150/w:150/czM6Ly9kaXNjb2dz/LWRhdGFiYXNlLWlt/YWdlcy9BLTI5NzM1/LTE3MjAwNjU1MTEt/NzM3Mi5qcGVn.jpeg"
        ),
        onClick = {}
    )
}