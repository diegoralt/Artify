package com.drkings.artify.data.datasource.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import com.drkings.artify.data.datasource.storage.entity.Artist
import com.drkings.artify.data.datasource.storage.entity.Pagination

@Database(entities = [Artist::class, Pagination::class], version = 2, exportSchema = false)
abstract class ArtifyDatabase : RoomDatabase() {
    abstract fun artistDao(): ArtistDao
    abstract fun paginationDao(): PaginationDao
}