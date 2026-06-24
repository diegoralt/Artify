package com.drkings.artify.data.datasource.storage.entity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["albumUuid", "genreUuid"],
    foreignKeys = [
        ForeignKey(
            entity = Album::class,
            parentColumns = ["uuid"],
            childColumns = ["albumUuid"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Genre::class,
            parentColumns = ["uuid"],
            childColumns = ["genreUuid"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AlbumGenreCrossRef(
    val albumUuid: String,
    val genreUuid: String
)
