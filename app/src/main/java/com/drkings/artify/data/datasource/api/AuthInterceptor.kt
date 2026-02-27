package com.drkings.artify.data.datasource.api

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val token: String, private val appVersion: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Discogs token=$token")
            .addHeader("User-Agent", "Artify/$appVersion")
            .build()
        return chain.proceed(request)
    }
}
