package com.drkings.artify.data.repository

import android.util.Log
import com.drkings.artify.data.datasource.api.ApiService
import com.drkings.artify.data.mapper.toDomain
import com.drkings.artify.domain.entity.ArtistDetailEntity
import com.drkings.artify.domain.repository.ArtistDetailRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class ArtistDetailRepositoryImpl @Inject constructor(val apiService: ApiService) :
    ArtistDetailRepository {

    override suspend fun getDetail(artistId: Int): ArtistDetailEntity {
        return try {
            coroutineScope {
                val response = apiService.getArtistDetail(artistId)

                if (response.members?.isNotEmpty() == true) {
                    val imageByArtistId: Map<Int, String> = response.members.map { member ->
                        async {
                            val artist = runCatching {
                                apiService.getArtistDetail(member.id)
                            }.getOrNull()

                            member.id to artist?.images?.find { it.type == "primary" }?.resourceUrl.orEmpty()
                        }
                    }.awaitAll().toMap()

                    response.toDomain(imageByArtistId)
                } else {
                    response.toDomain()
                }
            }
        } catch (e: Exception) {
            Log.e("ArtistDetailRepositoryImpl", "getDetail: ${e.message}")
            throw e
        }
    }
}
