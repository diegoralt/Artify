package com.drkings.artify.data.datasource.storage.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ArtistWithAlbum(
    @Embedded val artist: Artist,
    @Relation(
        parentColumn = "id",
        entityColumn = "artistId"
    )
    val albums: List<Album>
)
