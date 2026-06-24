package com.drkings.artify.data.datasource.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["search_query", "page"])])
data class Artist(
    @PrimaryKey val uuid: String,
    val id: Int,
    val type: String,
    val name: String,
    val thumb: String,
    val image: String? = null,
    val profile: String? = null,
    @ColumnInfo("search_query") val searchQuery: String,
    val page: Int,
    val createdAt: Long
)
