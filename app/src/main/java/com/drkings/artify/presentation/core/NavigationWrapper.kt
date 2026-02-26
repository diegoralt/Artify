package com.drkings.artify.presentation.core

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.drkings.artify.presentation.detail.AlbumsDetailScreen
import com.drkings.artify.presentation.detail.ArtistDetailScreen
import com.drkings.artify.presentation.search.SearchScreen

@Composable
fun NavigationWrapper() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Search) {
        composable<Search> {
            SearchScreen(navigateToDetails = { artistId ->
                navController.navigate(ArtistDetail(artistId))
            })
        }
        composable<ArtistDetail> {
            ArtistDetailScreen(navigateToBack = { navController.popBackStack() }, navigateToAlbums = { artistId ->
                navController.navigate(AlbumsDetail(artistId))
            })
        }
        composable<AlbumsDetail> {
            AlbumsDetailScreen(navigateToBack = { navController.popBackStack() })
        }
    }
}