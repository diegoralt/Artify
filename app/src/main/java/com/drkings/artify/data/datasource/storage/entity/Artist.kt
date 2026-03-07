package com.drkings.artify.data.datasource.storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Artist(
    @PrimaryKey val uuid: String,
    val id: Int,
    val type: String,
    val name: String,
    val thumb: String,
    val image: String? = null,
    val profile: String? = null,
    val createdAt: Long
)
