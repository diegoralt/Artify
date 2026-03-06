package com.drkings.artify

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.drkings.artify.presentation.core.NavigationWrapper
import com.drkings.artify.ui.theme.ArtifyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = false

        setContent {
            ArtifyTheme {
                NavigationWrapper(savedInstanceState == null && Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
            }
        }
    }
}
