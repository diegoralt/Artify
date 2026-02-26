package com.drkings.artify.data.datasource.api

import com.drkings.artify.data.response.ArtistDetailResponse
import com.drkings.artify.data.response.ArtistReleasesResponse
import com.drkings.artify.data.response.ReleaseDetailResponse
import com.drkings.artify.data.response.SearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("database/search")
    suspend fun search(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("type") type: String = "artist"
    ): SearchResponse

    @GET("artists/{id}")
    suspend fun getArtistDetail(
        @Path("id") id: Int
    ): ArtistDetailResponse

    @GET("artists/{id}/releases")
    suspend fun getArtistReleases(
        @Path("id") id: Int,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("sort") sort: String = "year",
        @Query("sort_order") sortOrder: String = "desc"
    ): ArtistReleasesResponse

    @GET("releases/{id}")
    suspend fun getReleaseDetail(
        @Path("id") id: Int
    ): ReleaseDetailResponse
}