package com.drkings.artify.data.datasource.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.drkings.artify.data.datasource.storage.entity.Artist

@Dao
interface ArtistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtists(artists: List<Artist>)

    @Query("SELECT * FROM artist WHERE search_query = :query AND page = :page LIMIT :perPage")
    suspend fun getArtists(query: String, page: Int, perPage: Int): List<Artist>

    @Query("DELETE FROM artist WHERE search_query = :query AND page = :page")
    suspend fun deleteArtistsByQueryAndPage(query: String, page: Int)
}
