package com.drkings.artify.presentation.core

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.drkings.artify.ui.theme.Green60
import com.drkings.artify.ui.theme.Neutral6

@Composable
fun LoadingContent() {
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
