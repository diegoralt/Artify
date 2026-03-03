package com.drkings.artify.presentation.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.drkings.artify.presentation.detail.AlbumsDetailScreen
import com.drkings.artify.presentation.detail.ArtistDetailScreen
import com.drkings.artify.presentation.search.SearchScreen
import com.drkings.artify.presentation.splash.SplashScreen

@Composable
fun NavigationWrapper(shouldShowSplash: Boolean) {
    val navController = rememberNavController()

    val navigateToSearch: () -> Unit = remember(navController) {
        {
            navController.navigate(Search) {
                popUpTo(Splash) { inclusive = true }
            }
        }
    }

    val navigateToArtist: (Int) -> Unit = remember(navController) {
        { artistId ->
            navController.navigate(ArtistDetail(artistId))
        }
    }

    val navigateToAlbums: (Int, String) -> Unit = remember(navController) {
        { artistId, artistName ->
            navController.navigate(AlbumsDetail(artistId, artistName))
        }
    }

    val navigateToBack: () -> Unit = remember(navController) {
        {
            navController.popBackStack()
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (shouldShowSplash) Splash else Search
    ) {
        composable<Splash> {
            SplashScreen(navigateToSearch = navigateToSearch)
        }
        composable<Search> {
            SearchScreen(navigateToDetails = navigateToArtist)
        }
        composable<ArtistDetail> {
            ArtistDetailScreen(
                navigateToBack = navigateToBack,
                navigateToAlbums = navigateToAlbums
            )
        }
        composable<AlbumsDetail> {
            AlbumsDetailScreen(navigateToBack = navigateToBack)
        }
    }
}
