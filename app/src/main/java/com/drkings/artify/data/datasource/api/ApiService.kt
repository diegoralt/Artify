package com.drkings.artify.data.datasource.api

import com.drkings.artify.data.response.SearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("database/search")
    suspend fun search(
        @Query("q") query: String,
        @Query("type") type: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): SearchResponse
}