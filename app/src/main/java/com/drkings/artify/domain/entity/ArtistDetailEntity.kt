package com.drkings.artify.domain.entity

data class ArtistDetailEntity(
    val id: Int,
    val name: String,
    val profile: String,
    val image: String,
    val members: List<MemberEntity>
)

data class MemberEntity(
    val name: String,
    val active: Boolean
)
