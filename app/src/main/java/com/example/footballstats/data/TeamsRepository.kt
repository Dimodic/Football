package com.example.footballstats.data

import com.example.footballstats.core.network.SStatsApi
import com.example.footballstats.core.util.DataResult
import com.example.footballstats.core.util.DateTimeUtils
import com.example.footballstats.domain.Player
import com.example.footballstats.domain.Team
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.time.Instant

class TeamsRepository(
    private val api: SStatsApi,
) {
    suspend fun getTeamsByLeague(leagueId: Int, year: Int): DataResult<List<Team>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.seasonTable(leagueId = leagueId, year = year)
                val teams = response.data.orEmpty()
                    .mapNotNull { row ->
                        val id = row.teamId
                        val name = row.teamName
                        if (id == null || name.isNullOrBlank()) null else Team(id = id, name = name)
                    }
                    .distinctBy { it.id }
                    .sortedBy { it.name }

                DataResult.Success(teams)
            } catch (t: Throwable) {
                if (t is HttpException && t.code() == 400) {
                    return@withContext deriveTeamsFromGames(leagueId)
                }

                val isNetwork = t is IOException
                val message = if (isNetwork) {
                    "Проблемы с сетью. Не удалось загрузить команды."
                } else {
                    t.message ?: "Failed to load teams"
                }
                DataResult.Error(message = message, isNetworkError = isNetwork)
            }
        }
    }

    private suspend fun deriveTeamsFromGames(leagueId: Int): DataResult<List<Team>> {
        return try {
            val tz = 0
            val now = Instant.now()
            val from = now.minusSeconds(370L * 24L * 3600L)
            val to = now.plusSeconds(370L * 24L * 3600L)

            val ended = api.gamesList(
                from = DateTimeUtils.formatApiDateTime(from, tz),
                to = DateTimeUtils.formatApiDateTime(to, tz),
                timezone = tz,
                leagueId = leagueId,
                ended = true,
                limit = 500,
                includeOdds = false,
            ).data.orEmpty()

            val upcoming = api.gamesList(
                from = DateTimeUtils.formatApiDateTime(from, tz),
                to = DateTimeUtils.formatApiDateTime(to, tz),
                timezone = tz,
                leagueId = leagueId,
                upcoming = true,
                limit = 500,
                includeOdds = false,
            ).data.orEmpty()

            val teams = (ended + upcoming)
                .flatMap { g -> listOf(g.homeTeam, g.awayTeam) }
                .mapNotNull { t ->
                    val id = t?.id?.toInt()
                    val name = t?.name
                    if (id == null || name.isNullOrBlank()) null else Team(id = id, name = name)
                }
                .distinctBy { it.id }
                .sortedBy { it.name }

            if (teams.isEmpty()) {
                DataResult.Error(
                    message = "Нет данных по этой лиге: турнирная таблица недоступна, и в матчах не нашлось команд.",
                    isNetworkError = false,
                )
            } else {
                DataResult.Success(
                    data = teams,
                    isFromCache = false,
                    notice = "Турнирная таблица недоступна для этой лиги — список команд собран по матчам.",
                )
            }
        } catch (t: Throwable) {
            val isNetwork = t is IOException
            val msg = if (isNetwork) {
                "Проблемы с сетью. Не удалось загрузить команды."
            } else {
                t.message ?: "Failed to load teams"
            }
            DataResult.Error(message = msg, isNetworkError = isNetwork)
        }
    }

    suspend fun getPlayersByTeamName(teamName: String, limit: Int = 100): DataResult<List<Player>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.playersFind(name = teamName, limit = limit)
                val players = response.data.orEmpty()
                    .mapNotNull { p ->
                        val name = p.name
                        if (name.isNullOrBlank()) return@mapNotNull null
                        Player(
                            id = p.id,
                            name = name,
                            position = p.position,
                            countryName = p.country?.name,
                        )
                    }
                    .let { list ->
                        
                        list
                    }
                DataResult.Success(players)
            } catch (t: Throwable) {
                val isNetwork = t is IOException
                val message = if (isNetwork) {
                    "Проблемы с сетью. Не удалось загрузить игроков."
                } else {
                    t.message ?: "Failed to load players"
                }
                DataResult.Error(message = message, isNetworkError = isNetwork)
            }
        }
    }
}
