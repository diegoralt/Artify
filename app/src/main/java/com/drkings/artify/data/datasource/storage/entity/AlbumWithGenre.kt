package com.drkings.artify.data.datasource.storage.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class AlbumWithGenre(
    @Embedded val album: Album,
    @Relation(
        parentColumn = "uuid",
        entityColumn = "uuid",
        associateBy = Junction(
            value = AlbumGenreCrossRef::class,
            parentColumn = "albumUuid",
            entityColumn = "genreUuid"
        )
    )
    val genres: List<Genre>
)
