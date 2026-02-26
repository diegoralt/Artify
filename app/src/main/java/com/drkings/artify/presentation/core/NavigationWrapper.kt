package com.drkings.artify.presentation.core

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.drkings.artify.presentation.detail.AlbumsDetailScreen
import com.drkings.artify.presentation.detail.ArtistDetailScreen
import com.drkings.artify.presentation.search.SearchScreen
import com.drkings.artify.presentation.splash.SplashScreen

@Composable
fun NavigationWrapper() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Splash) {
        composable<Splash> {
            SplashScreen(navigateToSearch = {
                navController.navigate(Search) {
                    popUpTo(Splash) {
                        inclusive = true
                    }
                }
            })
        }
        composable<Search> {
            SearchScreen(navigateToDetails = { artistId ->
                navController.navigate(ArtistDetail(artistId))
            })
        }
        composable<ArtistDetail> {
            ArtistDetailScreen(
                navigateToBack = { navController.popBackStack() },
                navigateToAlbums = { artistId, artistName ->
                    navController.navigate(AlbumsDetail(artistId, artistName))
                })
        }
        composable<AlbumsDetail> {
            AlbumsDetailScreen(navigateToBack = { navController.popBackStack() })
        }
    }
}