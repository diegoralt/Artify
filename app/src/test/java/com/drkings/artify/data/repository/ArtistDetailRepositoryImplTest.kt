package com.drkings.artify.data.repository

import com.drkings.artify.data.datasource.api.ApiService
import com.drkings.artify.data.datasource.api.response.ArtistDetailResponse
import com.drkings.artify.data.datasource.api.response.ImageResponse
import com.drkings.artify.data.datasource.api.response.MemberResponse
import com.drkings.artify.data.datasource.storage.ArtifyDatabase
import com.drkings.artify.data.datasource.storage.ArtistDao
import com.drkings.artify.data.datasource.storage.entity.Artist
import com.drkings.artify.data.datasource.storage.entity.ArtistWithMember
import com.drkings.artify.data.datasource.storage.entity.Member
import com.drkings.artify.domain.repository.ArtistDetailRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ArtistDetailRepositoryImplTest {

    private lateinit var apiService: ApiService
    private lateinit var database: ArtifyDatabase
    private lateinit var artistDao: ArtistDao
    private lateinit var repository: ArtistDetailRepository

    @Before
    fun setUp() {
        apiService = mockk()
        database = mockk()
        artistDao = mockk()

        every { database.artistDao() } returns artistDao
        coEvery { artistDao.getArtistWithMembers(any()) } returns null
        coEvery { artistDao.deleteMembersByArtistId(any()) } just Runs
        coEvery { artistDao.updateArtistDetail(any(), any(), any(), any()) } just Runs
        coEvery { artistDao.insertArtists(any()) } just Runs
        coEvery { artistDao.insertMembers(any()) } just Runs

        repository = ArtistDetailRepositoryImpl(
            apiService = apiService,
            database = database,
            transactionRunner = { block -> block() }
        )
    }

    @Test
    fun `getDetail - no cache and artist without members - returns entity without member`() = runTest {
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
        coEvery { artistDao.getArtistWithMembers(artistId) } returns null
        coEvery { apiService.getArtistDetail(artistId) } returns fakeResponse

        // When
        val result = repository.getDetail(artistId)

        // Then
        assertEquals(null, result.members)
        coVerify(exactly = 1) { apiService.getArtistDetail(artistId) }
    }

    @Test
    fun `getDetail - no cache and multiple images - returns primary image`() = runTest {
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
        coEvery { artistDao.getArtistWithMembers(artistId) } returns null
        coEvery { apiService.getArtistDetail(artistId) } returns fakeResponse

        // When
        val result = repository.getDetail(artistId)

        // Then
        assertEquals(urlImage, result.image)
    }

    @Test
    fun `getDetail - no cache with members - resolves member images in parallel`() = runTest {
        // Given
        val bandId = 29735
        val member1Id = 42610
        val member2Id = 530745

        val cachedArtist = Artist(
            uuid = "band-uuid",
            id = bandId,
            name = "Coldplay",
            type = "artist",
            thumb = "https://img.discogs.com/coldplay.jpg",
            searchQuery = "",
            page = 0,
            createdAt = 0L
        )

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

        coEvery { artistDao.getArtistWithMembers(bandId) } returns null
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
    fun `getDetail - no cache with members - maps member data correctly`() = runTest {
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

        coEvery { artistDao.getArtistWithMembers(bandId) } returns null
        coEvery { apiService.getArtistDetail(bandId) } returns bandResponse
        coEvery { apiService.getArtistDetail(member1Id) } returns member1Response
        coEvery { apiService.getArtistDetail(member2Id) } returns member2Response

        // When
        val result = repository.getDetail(bandId)

        // Then
        assertEquals(2, result.members?.size)
        val chrisMember = result.members?.first { it.id == member1Id }
        val guyMember = result.members?.first { it.id == member2Id }
        assertEquals("https://img.discogs.com/chris.jpg", chrisMember?.imageUrl)
        assertEquals("https://img.discogs.com/guy.jpg", guyMember?.imageUrl)
    }

    @Test
    fun `getDetail - valid cache with image - returns cached data without API call`() = runTest {
        // Given
        val artistId = 29735
        val cachedTimestamp = System.currentTimeMillis() - (12 * 60 * 60 * 1000L) // 12 horas atrás
        val cachedArtist = Artist(
            uuid = "cached-uuid",
            id = artistId,
            name = "Chris Martin",
            type = "artist",
            thumb = "https://img.discogs.com/chris.jpg",
            searchQuery = "",
            page = 0,
            image = "https://img.discogs.com/chris_martin.jpg",
            profile = "Chris Martin is an English singer",
            createdAt = cachedTimestamp
        )
        val cachedMembers = listOf(
            Member(
                uuid = "member-uuid-1",
                id = 1,
                name = "John",
                imageUrl = "https://img.discogs.com/john.jpg",
                artistId = artistId,
                createdAt = cachedTimestamp
            )
        )
        val cachedArtistWithMember = ArtistWithMember(
            artist = cachedArtist,
            members = cachedMembers
        )

        coEvery { artistDao.getArtistWithMembers(artistId) } returns cachedArtistWithMember

        // When
        val result = repository.getDetail(artistId)

        // Then
        // No debe llamar a la API porque el caché es válido (< 24h)
        coVerify(exactly = 0) { apiService.getArtistDetail(any()) }
        assertEquals("Chris Martin", result.name)
        assertEquals("https://img.discogs.com/chris_martin.jpg", result.image)
    }

    @Test
    fun `getDetail - expired cache - refreshes data and updates database`() = runTest {
        // Given
        val artistId = 29735
        val expiredTimestamp = System.currentTimeMillis() - (25 * 60 * 60 * 1000L) // 25 horas atrás
        val expiredArtist = Artist(
            uuid = "expired-uuid",
            id = artistId,
            name = "Old Name",
            type = "artist",
            thumb = "https://img.discogs.com/old.jpg",
            searchQuery = "",
            page = 0,
            image = "https://img.discogs.com/old.jpg",
            profile = "Old profile",
            createdAt = expiredTimestamp
        )
        val expiredArtistWithMember = ArtistWithMember(
            artist = expiredArtist,
            members = emptyList()
        )

        val freshResponse = ArtistDetailResponse(
            id = artistId,
            name = "Chris Martin",
            profile = "Updated profile",
            images = listOf(
                ImageResponse(
                    type = "primary",
                    resourceUrl = "https://img.discogs.com/chris.jpg"
                )
            ),
            members = null
        )

        coEvery { artistDao.getArtistWithMembers(artistId) } returns expiredArtistWithMember
        coEvery { apiService.getArtistDetail(artistId) } returns freshResponse

        // When
        val result = repository.getDetail(artistId)

        // Then
        // Debe llamar a la API porque el caché está expirado
        coVerify(exactly = 1) { apiService.getArtistDetail(artistId) }
        // Debe actualizar la BD
        coVerify(exactly = 1) { artistDao.updateArtistDetail(any(), any(), any(), any()) }
        assertEquals("Chris Martin", result.name)
        assertEquals("https://img.discogs.com/chris.jpg", result.image)
    }
}
