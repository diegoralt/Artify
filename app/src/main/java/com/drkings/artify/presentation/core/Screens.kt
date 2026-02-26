package com.drkings.artify.presentation.core

import kotlinx.serialization.Serializable

@Serializable
object Search

@Serializable
data class ArtistDetail(val artistId: Int)

@Serializable
data class AlbumsDetail(val artistId: Int)