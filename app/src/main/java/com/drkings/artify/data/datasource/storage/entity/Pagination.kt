package com.drkings.artify.data.datasource.storage.entity

import androidx.room.Entity

@Entity(primaryKeys = ["name", "page"])
data class Pagination(
    val name: String,
    val page: Int,
    val totalPages: Int,
    val totalItems: Int,
    val createdAt: Long
)
