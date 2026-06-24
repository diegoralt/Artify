package com.drkings.artify.data.datasource.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import com.drkings.artify.data.datasource.storage.entity.Album
import com.drkings.artify.data.datasource.storage.entity.AlbumGenreCrossRef
import com.drkings.artify.data.datasource.storage.entity.Artist
import com.drkings.artify.data.datasource.storage.entity.Genre
import com.drkings.artify.data.datasource.storage.entity.Member
import com.drkings.artify.data.datasource.storage.entity.Pagination

@Database(
    entities = [
        Artist::class,
        Pagination::class,
        Member::class,
        Album::class,
        Genre::class,
        AlbumGenreCrossRef::class
    ],
    version = 4,
    exportSchema = false
)
abstract class ArtifyDatabase : RoomDatabase() {
    abstract fun artistDao(): ArtistDao
    abstract fun paginationDao(): PaginationDao
    abstract fun releaseDao(): ReleaseDao
}
