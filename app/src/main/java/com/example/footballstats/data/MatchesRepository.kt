package com.example.footballstats.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.footballstats.core.network.SStatsApi
import com.example.footballstats.core.network.ApiGame
import com.example.footballstats.core.network.ApiOdds
import com.example.footballstats.core.util.DataResult
import com.example.footballstats.core.util.DateTimeUtils
import com.example.footballstats.domain.Match
import com.example.footballstats.domain.Odds1X2
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.first
import java.time.Instant

class MatchesRepository(
    private val api: SStatsApi,
    private val dataStore: DataStore<Preferences>,
    private val moshi: Moshi,
) {

    private val FINISHED_CACHE_JSON = stringPreferencesKey("finished_cache_json")
    private val FINISHED_CACHE_TS = longPreferencesKey("finished_cache_ts")

    suspend fun getUpcomingForNextDays(days: Int = 2): DataResult<List<Match>> {
        val now = Instant.now()
        val from = now
        val to = now.plusSeconds(days.toLong() * 24L * 3600L)
        val tz = 0 

        return try {
            val response = api.gamesList(
                from = DateTimeUtils.formatApiDateTime(from, tz),
                to = DateTimeUtils.formatApiDateTime(to, tz),
                timezone = tz,
                upcoming = true,
                limit = 100,
                includeOdds = true,
            )

            val matches = response.data.orEmpty().mapNotNull(::toDomainMatch)

            refreshFinishedCache(bestEffort = true)

            DataResult.Success(matches)
        } catch (t: Throwable) {
            val cached = loadFinishedCache()
            if (cached.isNotEmpty()) {
                DataResult.Success(
                    data = cached,
                    isFromCache = true,
                    notice = "Network issue: showing last 10 finished matches from cache."
                )
            } else {
                DataResult.Error(
                    message = t.message ?: "Something went wrong",
                    isNetworkError = true,
                )
            }
        }
    }

    suspend fun getMatches(
        leagueId: Int?,
        from: Instant,
        to: Instant,
        upcoming: Boolean,
        teamQuery: String? = null,
    ): DataResult<List<Match>> {
        val tz = 0
        return try {
            val response = api.gamesList(
                from = DateTimeUtils.formatApiDateTime(from, tz),
                to = DateTimeUtils.formatApiDateTime(to, tz),
                timezone = tz,
                leagueId = leagueId,
                upcoming = if (upcoming) true else null,
                ended = if (!upcoming) true else null,
                limit = 500,
                includeOdds = true,
            )

            val raw = response.data.orEmpty().mapNotNull(::toDomainMatch)
            val filtered = if (teamQuery.isNullOrBlank()) raw else {
                val q = teamQuery.trim().lowercase()
                raw.filter { m ->
                    m.homeTeamName.lowercase().contains(q) || m.awayTeamName.lowercase().contains(q)
                }
            }

            if (!upcoming) {
                updateFinishedCache(filtered)
            }

            DataResult.Success(filtered)
        } catch (t: Throwable) {
            DataResult.Error(
                message = t.message ?: "Something went wrong",
                isNetworkError = true,
            )
        }
    }

    suspend fun searchMatches(
        leagueId: Int?,
        rangeDays: Int,
        upcoming: Boolean,
        query: String? = null,
    ): DataResult<List<Match>> {
        val now = Instant.now()
        val from = if (upcoming) now.minusSeconds(1) else now.minusSeconds(rangeDays.toLong() * 24L * 3600L)
        val to = if (upcoming) now.plusSeconds(rangeDays.toLong() * 24L * 3600L) else now.plusSeconds(1)
        return getMatches(
            leagueId = leagueId,
            from = from,
            to = to,
            upcoming = upcoming,
            teamQuery = query,
        )
    }

    suspend fun getMatchDetails(matchId: Long): DataResult<Match> {
        return try {
            val response = api.gameDetails(matchId)
            val data = response.data ?: return DataResult.Error("Match not found")
            val game = data.game ?: return DataResult.Error("Match not found")

            val match = toDomainMatch(game) ?: return DataResult.Error("Match not found")
            val odds = mapToOdds1X2(data.odds)

            DataResult.Success(match.copy(odds = odds ?: match.odds))
        } catch (t: Throwable) {
            DataResult.Error(t.message ?: "Something went wrong")
        }
    }

    private suspend fun refreshFinishedCache(bestEffort: Boolean) {
        val now = Instant.now()
        val tz = 0
        val from = now.minusSeconds(30L * 24L * 3600L) 
        val to = now

        try {
            val response = api.gamesList(
                from = DateTimeUtils.formatApiDateTime(from, tz),
                to = DateTimeUtils.formatApiDateTime(to, tz),
                timezone = tz,
                ended = true,
                limit = 50,
                includeOdds = true,
            )
            val finished = response.data.orEmpty().mapNotNull(::toDomainMatch)
            updateFinishedCache(finished)
        } catch (t: Throwable) {
            if (!bestEffort) throw t
        }
    }

    private suspend fun updateFinishedCache(finishedMatches: List<Match>) {
        val dedup = finishedMatches
            .distinctBy { it.id }
            .sortedByDescending { it.startTime ?: Instant.EPOCH }
            .take(10)

        if (dedup.isEmpty()) return
        saveFinishedCache(dedup)
    }

    private suspend fun saveFinishedCache(matches: List<Match>) {
        val cached = matches.map { it.toCachedMatch() }
        val adapter = cachedMatchesAdapter(moshi)
        val json = adapter.toJson(cached)
        dataStore.edit { prefs ->
            prefs[FINISHED_CACHE_JSON] = json
            prefs[FINISHED_CACHE_TS] = System.currentTimeMillis()
        }
    }

    private suspend fun loadFinishedCache(): List<Match> {
        val prefs = dataStore.data.first()
        val json = prefs[FINISHED_CACHE_JSON] ?: return emptyList()
        val adapter = cachedMatchesAdapter(moshi)
        return adapter.fromJson(json).orEmpty().map { it.toMatch() }
    }

    private fun cachedMatchesAdapter(moshi: Moshi): JsonAdapter<List<CachedMatch>> {
        val type = Types.newParameterizedType(List::class.java, CachedMatch::class.java)
        return moshi.adapter(type)
    }

    private data class CachedMatch(
        val id: Long,
        val countryName: String,
        val leagueName: String,
        val homeTeamName: String,
        val awayTeamName: String,
        val startTimeEpochSeconds: Long?,
        val statusText: String,
        val win1: Double?,
        val drawX: Double?,
        val win2: Double?,
    )

    private fun Match.toCachedMatch(): CachedMatch {
        return CachedMatch(
            id = id,
            countryName = countryName,
            leagueName = leagueName,
            homeTeamName = homeTeamName,
            awayTeamName = awayTeamName,
            startTimeEpochSeconds = startTime?.epochSecond,
            statusText = statusText,
            win1 = odds?.win1,
            drawX = odds?.drawX,
            win2 = odds?.win2,
        )
    }

    private fun CachedMatch.toMatch(): Match {
        val odds = if (win1 == null && drawX == null && win2 == null) null else Odds1X2(win1, drawX, win2)
        return Match(
            id = id,
            countryName = countryName,
            leagueName = leagueName,
            homeTeamName = homeTeamName,
            awayTeamName = awayTeamName,
            startTime = startTimeEpochSeconds?.let { Instant.ofEpochSecond(it) },
            statusText = statusText,
            odds = odds,
        )
    }

    private fun toDomainMatch(game: ApiGame): Match? {
        val id = game.id ?: return null
        val homeName = game.homeTeam?.name ?: return null
        val awayName = game.awayTeam?.name ?: return null

        val leagueName = game.season?.league?.name ?: game.season?.league?.country?.name ?: "Unknown League"
        val countryName = game.season?.league?.country?.name ?: "Unknown"

        val start = DateTimeUtils.parseApiDateTime(game.date)
            ?: DateTimeUtils.parseEpochSeconds(game.dateUtc)

        val odds = mapToOdds1X2(game.odds) ?: run {
            if (game.winner1 != null || game.winnerX != null || game.winner2 != null) {
                Odds1X2(win1 = game.winner1, drawX = game.winnerX, win2 = game.winner2)
            } else null
        }

        return Match(
            id = id,
            countryName = countryName,
            leagueName = leagueName,
            homeTeamName = homeName,
            awayTeamName = awayName,
            startTime = start,
            statusText = game.statusName ?: "",
            odds = odds,
        )
    }

    private fun mapToOdds1X2(any: Any?): Odds1X2? {
        return when (any) {
            null -> null
            is ApiOdds -> Odds1X2(any.winner1, any.winnerX, any.winner2)
            is Map<*, *> -> {
                val w1 = (any["winner1"] as? Number)?.toDouble()
                val wx = (any["winnerX"] as? Number)?.toDouble()
                val w2 = (any["winner2"] as? Number)?.toDouble()
                if (w1 == null && wx == null && w2 == null) null else Odds1X2(w1, wx, w2)
            }
            is List<*> -> {
                
                val first = any.firstOrNull { it is Map<*, *> } as? Map<*, *>
                mapToOdds1X2(first)
            }
            else -> null
        }
    }
}
