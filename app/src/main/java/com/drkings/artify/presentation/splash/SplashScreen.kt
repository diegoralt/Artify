package com.drkings.artify.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drkings.artify.R
import com.drkings.artify.ui.theme.Green50
import com.drkings.artify.ui.theme.Green60
import com.drkings.artify.ui.theme.Neutral15
import com.drkings.artify.ui.theme.Neutral6
import com.drkings.artify.ui.theme.NeutralVariant60
import com.drkings.artify.ui.theme.NeutralVariant90
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navigateToSearch: () -> Unit
) {
    val logoScale = remember { Animatable(0.6f) }
    val logoAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Animar logo hacia adentro con spring overshoot (igual al mockup)
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
        // Navegar a SearchScreen tras 1.8s total en pantalla
        delay(1000L)
        // Fade out antes de navegar
        logoAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        )
        navigateToSearch()
    }

    LaunchedEffect(Unit) {
        logoAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Neutral6),
        contentAlignment = Alignment.Center
    ) {
        GlowBackground()

        Column(
            modifier = Modifier
                .scale(logoScale.value)
                .alpha(logoAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Green60)
                    .border(1.dp, Green50, RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_music_note),
                    contentDescription = null,
                    tint = Neutral15.copy(alpha = 0.7f),
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.app_name),
                fontSize = 36.sp,
                fontWeight = FontWeight.SemiBold,
                color = NeutralVariant90,
                letterSpacing = (-1).sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.splash_screen_subtitle),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = NeutralVariant60,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun GlowBackground() {
    Canvas(
        modifier = Modifier
            .size(300.dp)
            .offset(y = (-80).dp)
    ) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Green60.copy(alpha = 0.18f),
                    Color.Transparent
                ),
                radius = size.minDimension / 2f
            )
        )
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────
@Preview(showBackground = true, backgroundColor = 0xFF060E0B)
@Composable
private fun SplashScreenPreview() {
    SplashScreen(navigateToSearch = {})
}
