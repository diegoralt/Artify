package com.drkings.artify.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drkings.artify.presentation.model.Artist
import com.drkings.artify.presentation.model.ArtistType
import com.drkings.artify.ui.theme.Blue80
import com.drkings.artify.ui.theme.Green60
import com.drkings.artify.ui.theme.NeutralVariant20
import com.drkings.artify.ui.theme.NeutralVariant40
import com.drkings.artify.ui.theme.NeutralVariant60
import com.drkings.artify.ui.theme.NeutralVariant90

@Composable
fun ArtistResultItem(
    artist: Artist,
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
            ArtistTypeChip(type = artist.type)
        }

        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = NeutralVariant40,
            modifier = Modifier
                .size(18.dp)
                .alpha(0.4f)
        )
    }
}

@Composable
private fun ArtistAvatar(artist: Artist) {
    val avatarGradient = when (artist.type) {
        ArtistType.BAND -> Brush.linearGradient(
            colors = listOf(Color(0xFF1a3a2a), Color(0xFF2a5a3a)),
            start = Offset(0f, 0f), end = Offset(52f, 52f)
        )

        ArtistType.ARTIST -> Brush.linearGradient(
            colors = listOf(Color(0xFF1a2a3a), Color(0xFF2a3a5a)),
            start = Offset(0f, 0f), end = Offset(52f, 52f)
        )
    }
    val fallbackIcon =
        if (artist.type == ArtistType.BAND) Icons.Default.Person else Icons.Default.Person

    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(avatarGradient),
        contentAlignment = Alignment.Center
    ) {
        /*if (artist.thumbUrl != null) {
            AsyncImage(
                model = artist.thumbUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {*/
        Icon(
            imageVector = fallbackIcon,
            contentDescription = null,
            tint = NeutralVariant60,
            modifier = Modifier.size(26.dp)
        )
//        }
    }
}

@Composable
private fun ArtistTypeChip(type: ArtistType) {
    val (bg, textColor, label) = when (type) {
        ArtistType.BAND -> Triple(Green60.copy(alpha = 0.12f), Green60, "BAND")
        ArtistType.ARTIST -> Triple(Blue80.copy(alpha = 0.12f), Blue80, "ARTIST")
    }
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
private fun ArtistResultItemBandPreview() {
    ArtistResultItem(
        artist = Artist(
            name = "The Beatles",
            type = ArtistType.BAND,
            thumbUrl = null
        ),
        onClick = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF060E0B)
@Composable
private fun ArtistResultItemSoloPreview() {
    ArtistResultItem(
        artist = Artist(
            name = "John Lennon",
            type = ArtistType.ARTIST,
            thumbUrl = null
        ),
        onClick = {}
    )
}