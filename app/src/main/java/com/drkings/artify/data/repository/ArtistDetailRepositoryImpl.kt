package com.drkings.artify.data.repository

import android.util.Log
import com.drkings.artify.data.datasource.api.ApiService
import com.drkings.artify.data.datasource.storage.ArtifyDatabase
import com.drkings.artify.data.datasource.storage.entity.Artist
import com.drkings.artify.data.datasource.storage.entity.ArtistWithMember
import com.drkings.artify.data.mapper.toData
import com.drkings.artify.data.mapper.toDomain
import com.drkings.artify.domain.entity.ArtistDetailEntity
import com.drkings.artify.domain.repository.ArtistDetailRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.UUID

private const val ONE_DAY_MILLIS = 24 * 60 * 60 * 1000L

class ArtistDetailRepositoryImpl(
    private val apiService: ApiService,
    private val database: ArtifyDatabase,
    private val transactionRunner: TransactionRunner
) : ArtistDetailRepository {

    override suspend fun getDetail(artistId: Int): ArtistDetailEntity {
        val artistDao = database.artistDao()

        // cached contiene la fila con mayor prioridad: image != null primero,
        // luego la más reciente. Su uuid identifica unívocamente la fila a actualizar.
        val cached = artistDao.getArtistWithMembers(artistId)
        val isCacheValid = cached?.artist?.image != null &&
            (System.currentTimeMillis() - cached.artist.createdAt) < ONE_DAY_MILLIS

        if (isCacheValid) {
            return cached.toDomain()
        }

        return try {
            coroutineScope {
                val response = apiService.getArtistDetail(artistId)
                val image = response.images?.find { it.type == "primary" }?.resourceUrl.orEmpty()

                val imageByArtistId: Map<Int, String> =
                    if (!response.members.isNullOrEmpty()) {
                        response.members.map { member ->
                            async {
                                val memberDetail = runCatching {
                                    apiService.getArtistDetail(member.id)
                                }.getOrNull()

                                member.id to memberDetail?.images?.find { it.type == "primary" }?.resourceUrl.orEmpty()
                            }
                        }.awaitAll().toMap()
                    } else {
                        emptyMap()
                    }

                val members = response.members
                    ?.map { it.toData(artistId, imageByArtistId[it.id].orEmpty()) }
                    ?: emptyList()

                val cachedAt = System.currentTimeMillis()
                val artistUuid = cached?.artist?.uuid ?: UUID.randomUUID().toString()

                transactionRunner {
                    artistDao.deleteMembersByArtistId(artistId)
                    if (cached != null) {
                        // Actualizar el registro existente
                        artistDao.updateArtistDetail(
                            uuid = cached.artist.uuid,
                            image = image,
                            profile = response.profile,
                            createdAt = cachedAt
                        )
                    } else {
                        // Insertar nuevo registro
                        artistDao.insertArtists(
                            listOf(
                                Artist(
                                    uuid = artistUuid,
                                    id = artistId,
                                    type = "artist",
                                    name = response.name,
                                    thumb = image,
                                    image = image,
                                    profile = response.profile,
                                    searchQuery = "",
                                    page = 0,
                                    createdAt = cachedAt
                                )
                            )
                        )
                    }
                    artistDao.insertMembers(members)
                }

                ArtistWithMember(
                    artist = (cached?.artist ?: Artist(
                        uuid = artistUuid,
                        id = artistId,
                        type = "artist",
                        name = response.name,
                        thumb = image,
                        image = image,
                        profile = response.profile,
                        searchQuery = "",
                        page = 0,
                        createdAt = 0L
                    )).copy(
                        name = response.name,
                        image = image,
                        profile = response.profile,
                        createdAt = cachedAt
                    ),
                    members = members
                ).toDomain()
            }
        } catch (e: Exception) {
            Log.e("ArtistDetailRepositoryImpl", "getDetail: ${e.message}")
            throw e
        }
    }
}
