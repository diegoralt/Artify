package com.drkings.artify.data.datasource.storage.entity

import androidx.room.Database
import androidx.room.RoomDatabase
import com.drkings.artify.data.datasource.storage.ArtistDao

@Database(entities = [Artist::class], version = 1, exportSchema = false)
abstract class ArtifyDatabase : RoomDatabase() {
    abstract fun artistDao(): ArtistDao
}