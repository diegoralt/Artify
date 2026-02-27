package com.drkings.artify.presentation.search

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drkings.artify.ui.theme.Neutral15
import com.drkings.artify.ui.theme.Neutral20
import com.drkings.artify.ui.theme.NeutralVariant20

@Composable
fun SkeletonItem(modifier: Modifier = Modifier) {
    val shimmerBrush = rememberShimmerBrush()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 20.dp)
            .drawBottomBorder(NeutralVariant20),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Avatar placeholder
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(shimmerBrush)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Línea larga — nombre
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.60f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(shimmerBrush)
            )
            // Línea corta — subtítulo
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.42f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush)
            )
        }
    }
}

// Shimmer reutilizable: gradiente animado sin librerías externas
@Composable
private fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -300f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    return Brush.linearGradient(
        colors = listOf(Neutral15, Neutral20, Neutral15),
        start = Offset(translateAnim, 0f),
        end = Offset(translateAnim + 300f, 0f)
    )
}

// Reutiliza la misma extension de ArtistResultItem — mover a Modifier.kt si el proyecto crece
private fun Modifier.drawBottomBorder(color: Color): Modifier = this.drawWithContent {
    drawContent()
    drawLine(
        color = color,
        start = Offset(0f, size.height),
        end = Offset(size.width, size.height),
        strokeWidth = 1.dp.toPx()
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF060E0B)
@Composable
private fun SkeletonItemPreview() {
    Column {
        repeat(3) { SkeletonItem() }
    }
}