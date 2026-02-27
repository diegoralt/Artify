package com.drkings.artify.data.di

import com.drkings.artify.BuildConfig
import com.drkings.artify.data.datasource.api.ApiService
import com.drkings.artify.data.datasource.api.AuthInterceptor
import com.drkings.artify.data.repository.ArtistDetailRepositoryImpl
import com.drkings.artify.data.repository.ArtistReleasesRepositoryImpl
import com.drkings.artify.data.repository.SearchRepositoryImpl
import com.drkings.artify.domain.repository.ArtistDetailRepository
import com.drkings.artify.domain.repository.ArtistReleasesRepository
import com.drkings.artify.domain.repository.SearchRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(BuildConfig.DISCOGS_TOKEN, BuildConfig.VERSION_NAME))
            .build()
    }

    @Provides
    fun provideRetrofit(json: Json, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://api.discogs.com/")
            .addConverterFactory(json.asConverterFactory("application/json; charset=utf-8".toMediaType()))
            .build()
    }

    @Provides
    fun provideApiServices(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    fun provideSearchRepository(apiService: ApiService): SearchRepository {
        return SearchRepositoryImpl(apiService)
    }

    @Provides
    fun provideArtistDetailRepository(apiService: ApiService): ArtistDetailRepository {
        return ArtistDetailRepositoryImpl(apiService)
    }

    @Provides
    fun provideArtistReleasesRepository(apiService: ApiService): ArtistReleasesRepository {
        return ArtistReleasesRepositoryImpl(apiService)
    }
}
