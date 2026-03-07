package com.drkings.artify.data.datasource.storage.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class AlbumWithGenre(
    @Embedded val album: Album,
    @Relation(
        parentColumn = "albumId",
        entityColumn = "genreId",
        associateBy = Junction(AlbumGenreCrossRef::class)
    )
    val genres: List<Genre>
)
