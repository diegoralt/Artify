package com.drkings.artify.data.datasource.storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Album(
    @PrimaryKey val uuid: String,
    val id: Int,
    val title: String,
    val artist: String?,
    val year: Int?,
    val thumb: String?,
    val format: String?,
    val label: String?,
    val createdAt: Long
)
