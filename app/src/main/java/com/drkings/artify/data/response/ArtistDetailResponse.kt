package com.drkings.artify.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArtistDetailResponse(
    val id: Int,
    val name: String,
    val profile: String,
    val images: List<ImageResponse>,
    val members: List<MemberResponse>
)

@Serializable
data class ImageResponse(
    val type: String,
    @SerialName("resource_url")
    val resourceUrl: String,
)

@Serializable
data class MemberResponse(
    val id: Int,
    val name: String,
    val active: Boolean,
    @SerialName("thumbnail_url") val thumbnailUrl: String?
)



