package com.drkings.artify.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArtistDetailResponse(
    val id: Int,
    val name: String,
    val profile: String,
    val images: List<ImageResponse>,
    val members: List<MemberResponse>? = null
)

@Serializable
data class MemberResponse(
    val id: Int,
    val name: String,
    @SerialName("resource_url")
    val resourceUrl: String
)



