package com.example.footballstats.core.network

import okhttp3.Interceptor
import okhttp3.Response

class ApiKeyInterceptor(
    private val apiKey: String,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        if (apiKey.isBlank()) {
            return chain.proceed(original)
        }

        val newUrl = original.url.newBuilder()
            .addQueryParameter("apikey", apiKey)
            .build()

        val newRequest = original.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}
