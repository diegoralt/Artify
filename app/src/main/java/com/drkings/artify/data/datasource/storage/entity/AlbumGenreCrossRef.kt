package com.drkings.artify.data.datasource.storage.entity

import androidx.room.Entity

@Entity(primaryKeys = ["albumId", "genreId"])
data class AlbumGenreCrossRef(
    val albumId: Int,
    val genreId: Int
)
