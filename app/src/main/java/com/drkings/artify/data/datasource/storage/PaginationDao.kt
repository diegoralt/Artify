package com.drkings.artify.data.datasource.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.drkings.artify.data.datasource.storage.entity.Pagination

@Dao
interface PaginationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPagination(cache: Pagination)

    @Query("SELECT * FROM pagination WHERE name = :query AND page = :page LIMIT 1")
    suspend fun getPagination(query: String, page: Int): Pagination?

    @Query("DELETE FROM pagination WHERE name = :query AND page = :page")
    suspend fun deletePaginationByQueryAndPage(query: String, page: Int)

}
