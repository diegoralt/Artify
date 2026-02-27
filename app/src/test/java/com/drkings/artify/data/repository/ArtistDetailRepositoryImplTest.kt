package com.drkings.artify.data.repository

import com.drkings.artify.data.datasource.api.ApiService
import com.drkings.artify.data.response.ArtistDetailResponse
import com.drkings.artify.data.response.ImageResponse
import com.drkings.artify.data.response.MemberResponse
import com.drkings.artify.domain.repository.ArtistDetailRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ArtistDetailRepositoryImplTest {

    private lateinit var apiService: ApiService
    private lateinit var repository: ArtistDetailRepository

    @Before
    fun setUp() {
        apiService = mockk()
        repository = ArtistDetailRepositoryImpl(apiService)
    }

    @Test
    fun `getDetail - artist without members - returns entity without member`() = runTest {

        // Given
        val artistId = 29735
        val fakeResponse = ArtistDetailResponse(
            id = artistId,
            name = "Chris Martin",
            profile = "Chris Martin is an English singer",
            images = listOf(
                ImageResponse(
                    type = "primary",
                    resourceUrl = "https://img.discogs.com/chris_martin.jpg"
                )
            )
        )
        coEvery { apiService.getArtistDetail(artistId) } returns fakeResponse

        // When
        val result = repository.getDetail(artistId)

        // Then
        assertEquals(null, result.members)

        // getArtistDetail solo debe haberse llamado una vez
        coVerify(exactly = 1) { apiService.getArtistDetail(any()) }
    }

    @Test
    fun `getDetail - artist with different images - returns entity with primary type image`() =
        runTest {

            // Given
            val artistId = 29735
            val urlImage = "https://img.discogs.com/chris_martin.jpg"
            val fakeResponse = ArtistDetailResponse(
                id = artistId,
                name = "Chris Martin",
                profile = "Chris Martin is an English singer",
                images = listOf(
                    ImageResponse(
                        type = "primary",
                        resourceUrl = urlImage
                    ),
                    ImageResponse(
                        type = "secondary",
                        resourceUrl = "https://img.discogs.com/chris.jpg"
                    )
                ),
                members = null
            )
            coEvery { apiService.getArtistDetail(artistId) } returns fakeResponse

            // When
            val result = repository.getDetail(artistId)

            // Then
            assertEquals(urlImage, result.image)
        }

    @Test
    fun `getDetail - artist with members - resolves member images in parallel`() = runTest {

        // Given
        val bandId = 29735
        val member1Id = 42610
        val member2Id = 530745

        val bandResponse = ArtistDetailResponse(
            id = bandId,
            name = "Coldplay",
            profile = "Pop rock band from London.",
            images = listOf(
                ImageResponse(
                    type = "primary",
                    resourceUrl = "https://img.discogs.com/coldplay.jpg"
                )
            ),
            members = listOf(
                MemberResponse(
                    id = member1Id,
                    name = "Chris Martin",
                    resourceUrl = "https://img.discogs.com/chris.jpg"
                ),
                MemberResponse(
                    id = member2Id,
                    name = "Guy Berryman",
                    resourceUrl = "https://img.discogs.com/guy.jpg"
                )
            )
        )

        val member1Response = ArtistDetailResponse(
            id = member1Id,
            name = "Chris Martin",
            profile = "",
            images = listOf(
                ImageResponse(
                    type = "primary",
                    resourceUrl = "https://img.discogs.com/chris.jpg"
                )
            )
        )

        val member2Response = ArtistDetailResponse(
            id = member2Id,
            name = "Guy Berryman",
            profile = "",
            images = listOf(
                ImageResponse(type = "primary", resourceUrl = "https://img.discogs.com/guy.jpg")
            )
        )

        coEvery { apiService.getArtistDetail(bandId) } returns bandResponse
        coEvery { apiService.getArtistDetail(member1Id) } returns member1Response
        coEvery { apiService.getArtistDetail(member2Id) } returns member2Response

        // When
        val result = repository.getDetail(bandId)

        // Then
        // getArtistDetail debe haberse llamado 3 veces
        coVerify(exactly = 1) { apiService.getArtistDetail(bandId) }
        coVerify(exactly = 1) { apiService.getArtistDetail(member1Id) }
        coVerify(exactly = 1) { apiService.getArtistDetail(member2Id) }
    }

    @Test
    fun `getDetail - artist with members - resolves member response`() = runTest {

        // Given
        val bandId = 29735
        val member1Id = 42610
        val member2Id = 530745

        val bandResponse = ArtistDetailResponse(
            id = bandId,
            name = "Coldplay",
            profile = "Pop rock band from London.",
            images = listOf(
                ImageResponse(
                    type = "primary",
                    resourceUrl = "https://img.discogs.com/coldplay.jpg"
                )
            ),
            members = listOf(
                MemberResponse(
                    id = member1Id,
                    name = "Chris Martin",
                    resourceUrl = "https://img.discogs.com/chris.jpg"
                ),
                MemberResponse(
                    id = member2Id,
                    name = "Guy Berryman",
                    resourceUrl = "https://img.discogs.com/guy.jpg"
                )
            )
        )

        val member1Response = ArtistDetailResponse(
            id = member1Id,
            name = "Chris Martin",
            profile = "",
            images = listOf(
                ImageResponse(
                    type = "primary",
                    resourceUrl = "https://img.discogs.com/chris.jpg"
                ),
                ImageResponse(
                    type = "secondary",
                    resourceUrl = "https://img.discogs.com/chris_martin.jpg"
                )
            )
        )

        val member2Response = ArtistDetailResponse(
            id = member2Id,
            name = "Guy Berryman",
            profile = "",
            images = listOf(
                ImageResponse(
                    type = "primary",
                    resourceUrl = "https://img.discogs.com/guy.jpg"
                ),
                ImageResponse(
                    type = "secondary",
                    resourceUrl = "https://img.discogs.com/guy_berryman.jpg"
                )
            )
        )

        coEvery { apiService.getArtistDetail(bandId) } returns bandResponse
        coEvery { apiService.getArtistDetail(member1Id) } returns member1Response
        coEvery { apiService.getArtistDetail(member2Id) } returns member2Response

        // When
        val result = repository.getDetail(bandId)

        // Then
        // La entidad de la banda debe tener los datos correctos
        assertEquals(2, result.members?.size)

        // Las imágenes de los miembros deben haberse resuelto correctamente
        val chrisMember = result.members?.first { it.id == member1Id }
        val guyMember = result.members?.first { it.id == member2Id }
        assertEquals("https://img.discogs.com/chris.jpg", chrisMember?.imageUrl)
        assertEquals("https://img.discogs.com/guy.jpg", guyMember?.imageUrl)
    }
}