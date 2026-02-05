package com.example.footballstats.core.util

sealed interface DataResult<out T> {
    data class Success<T>(
        val data: T,
        val isFromCache: Boolean = false,
        val notice: String? = null,
    ) : DataResult<T>

    data class Error<T>(
        val message: String,
        val cachedData: T? = null,
        val isNetworkError: Boolean = false,
    ) : DataResult<T>
}