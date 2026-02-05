package com.example.footballstats.data

import com.example.footballstats.core.network.SStatsApi
import com.example.footballstats.core.util.DataResult
import com.example.footballstats.domain.League
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class LeaguesRepository(
    private val api: SStatsApi
) {
    suspend fun getLeagues(): DataResult<List<League>> = withContext(Dispatchers.IO) {
        try {
            val resp = api.leagues()
            val mapped = resp.data.orEmpty()
                .mapNotNull { dto ->
                    val id = dto.id ?: return@mapNotNull null
                    League(
                        id = id,
                        name = dto.name ?: "League $id",
                        countryName = dto.country?.name
                    )
                }
                .sortedBy { it.name }

            DataResult.Success(mapped)
        } catch (e: Exception) {
            DataResult.Error(e.toUserMessage())
        }
    }

    private fun Exception.toUserMessage(): String = when (this) {
        is IOException -> "Network error. Check your connection."
        is HttpException -> "Server error: ${code()}"
        else -> "Unexpected error: ${message ?: this::class.simpleName}"
    }
}