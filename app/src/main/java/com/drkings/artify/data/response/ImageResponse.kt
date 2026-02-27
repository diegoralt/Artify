package com.drkings.artify.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ImageResponse(
    val type: String,
    @SerialName("resource_url")
    val resourceUrl: String
)
