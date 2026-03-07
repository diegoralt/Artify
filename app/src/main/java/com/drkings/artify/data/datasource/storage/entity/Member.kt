package com.drkings.artify.data.datasource.storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Member(
    @PrimaryKey val uuid: String,
    val id: Int,
    val name: String,
    val imageUrl: String,
    val artistId: Int,
    val createdAt: Long
)
