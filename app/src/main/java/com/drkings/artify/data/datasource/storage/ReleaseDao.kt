package com.drkings.artify.data.datasource.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.drkings.artify.data.datasource.storage.entity.Album
import com.drkings.artify.data.datasource.storage.entity.AlbumGenreCrossRef
import com.drkings.artify.data.datasource.storage.entity.AlbumWithGenre
import com.drkings.artify.data.datasource.storage.entity.Genre

@Dao
interface ReleaseDao {

    // ── Album flow ───────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbums(albums: List<Album>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenres(genres: List<Genre>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbumGenreCrossRefs(refs: List<AlbumGenreCrossRef>)

    @Transaction
    @Query(
        """
        SELECT * FROM album
        WHERE artistId = :artistId AND page = :page
        ORDER BY createdAt DESC
    """
    )
    suspend fun getAlbumsWithGenres(artistId: Int, page: Int): List<AlbumWithGenre>

    @Query("DELETE FROM album WHERE artistId = :artistId AND page = :page")
    suspend fun deleteAlbumsByArtistAndPage(artistId: Int, page: Int)

    @Query("DELETE FROM albumgenrecrossref WHERE albumUuid IN (SELECT uuid FROM album WHERE artistId = :artistId AND page = :page)")
    suspend fun deleteGenreRelationsByArtistAndPage(artistId: Int, page: Int)

    @Query("DELETE FROM genre WHERE uuid NOT IN (SELECT DISTINCT genreUuid FROM albumgenrecrossref)")
    suspend fun deleteOrphanedGenres()
}
