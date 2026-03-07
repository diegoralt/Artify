package com.drkings.artify.data.datasource.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.drkings.artify.data.datasource.storage.entity.Artist
import com.drkings.artify.data.datasource.storage.entity.Member

@Dao
interface ArtistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtists(artists: List<Artist>)

    @Insert
    suspend fun insertArtistAndMembers(artist: Artist, members: List<Member>)

    @Query("SELECT * FROM artist WHERE name LIKE :query || '%'")
    suspend fun getArtists(query: String): List<Artist>

    @Query("DELETE FROM artist")
    suspend fun deleteAllArtists()

}