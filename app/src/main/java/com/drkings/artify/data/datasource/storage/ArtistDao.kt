package com.drkings.artify.data.datasource.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.drkings.artify.data.datasource.storage.entity.Artist
import com.drkings.artify.data.datasource.storage.entity.ArtistWithMember
import com.drkings.artify.data.datasource.storage.entity.Member

@Dao
interface ArtistDao {

    // ── Search flow ──────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtists(artists: List<Artist>)

    @Query("SELECT * FROM artist WHERE search_query = :query AND page = :page LIMIT :perPage")
    suspend fun getArtists(query: String, page: Int, perPage: Int): List<Artist>

    @Query("DELETE FROM artist WHERE search_query = :query AND page = :page")
    suspend fun deleteArtistsByQueryAndPage(query: String, page: Int)

    // ── Artist detail flow ───────────────────────────────────────────────────

    /**
     * Retorna el artista con sus miembros priorizando la fila que tenga image registrada
     * (detalle previamente cacheado), usando createdAt como criterio de desempate.
     */
    @Transaction
    @Query(
        """
        SELECT * FROM artist
        WHERE id = :artistId
        ORDER BY (image IS NOT NULL) DESC, createdAt DESC
        LIMIT 1
    """
    )
    suspend fun getArtistWithMembers(artistId: Int): ArtistWithMember?

    /**
     * Actualiza únicamente la fila identificada por [uuid], preservando el resto de
     * registros del mismo artista que pertenecen al caché de búsqueda.
     */
    @Query("UPDATE artist SET image = :image, profile = :profile, createdAt = :createdAt WHERE uuid = :uuid")
    suspend fun updateArtistDetail(uuid: String, image: String, profile: String, createdAt: Long)

    // ── Member flow ──────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<Member>)

    @Query("DELETE FROM member WHERE artistId = :artistId")
    suspend fun deleteMembersByArtistId(artistId: Int)
}
