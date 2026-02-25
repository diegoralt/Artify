package com.drkings.artify.presentation.core

import kotlinx.serialization.Serializable

@Serializable
object Search

@Serializable
data class ArtistDetail(val idArtist: Int)

@Serializable
object AlbumsDetails