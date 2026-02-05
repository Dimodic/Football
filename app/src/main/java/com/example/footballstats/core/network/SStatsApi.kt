package com.example.footballstats.core.network

import com.squareup.moshi.Json
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SStatsApi {
    @GET("Leagues")
    suspend fun leagues(): ApiResponse<List<ApiLeague>>

    @GET("Games/season-table")
    suspend fun seasonTable(
        @Query("league") leagueId: Int,
        @Query("year") year: Int,
    ): ApiResponse<List<ApiTableRow>>

    @GET("Players/find")
    suspend fun playersFind(
        @Query("name") name: String,
        @Query("limit") limit: Int = 50,
    ): ApiResponse<List<ApiPlayer>>

    @GET("Games/list")
    suspend fun gamesList(
        
        
        @Query("From") from: String,
        @Query("To") to: String,
        @Query("TimeZone") timezone: Int,
        @Query("LeagueId") leagueId: Int? = null,
        @Query("Upcoming") upcoming: Boolean? = null,
        @Query("Ended") ended: Boolean? = null,
        
        @Query("TeamId") teamId: Int? = null,
        @Query("Live") live: Boolean? = null,
        @Query("Limit") limit: Int = 100,
        @Query("IncludeOdds") includeOdds: Boolean = true,
    ): ApiResponse<List<ApiGame>>

    @GET("Games/{id}")
    suspend fun gameDetails(
        @Path("id") id: Long,
    ): ApiResponse<ApiGameDetailsData>

    @GET("Games/glicko/{id}")
    suspend fun gameGlicko(
        @Path("id") id: Long,
    ): ApiResponse<ApiGameGlickoData>
}

data class ApiResponse<T>(
    @param:Json(name = "status") val status: String? = null,
    @param:Json(name = "data") val data: T? = null,
)

data class ApiCountry(
    @param:Json(name = "name") val name: String? = null,
    @param:Json(name = "code") val code: String? = null,
)

data class ApiLeagueSeason(
    @param:Json(name = "uid") val uid: String? = null,
    @param:Json(name = "year") val year: Int? = null,
    @param:Json(name = "dateStart") val dateStart: String? = null,
    @param:Json(name = "dateEnd") val dateEnd: String? = null,
    @param:Json(name = "flashScoreId") val flashScoreId: String? = null,
)

data class ApiLeague(
    @param:Json(name = "id") val id: Int? = null,
    @param:Json(name = "name") val name: String? = null,
    @param:Json(name = "flashScoreId") val flashScoreId: String? = null,
    @param:Json(name = "country") val country: ApiCountry? = null,
    @param:Json(name = "seasons") val seasons: List<ApiLeagueSeason>? = null,
)

data class ApiTeam(
    @param:Json(name = "id") val id: Long? = null,
    @param:Json(name = "name") val name: String? = null,
    @param:Json(name = "flashId") val flashId: String? = null,
)

data class ApiGameSeason(
    @param:Json(name = "uid") val uid: String? = null,
    @param:Json(name = "id") val id: Long? = null,
    @param:Json(name = "year") val year: Int? = null,
    @param:Json(name = "league") val league: ApiLeague? = null,
    @param:Json(name = "roundName") val roundName: String? = null,
)

data class ApiGame(
    @param:Json(name = "id") val id: Long? = null,
    @param:Json(name = "flashId") val flashId: String? = null,
    @param:Json(name = "dateUtc") val dateUtc: Any? = null,
    @param:Json(name = "date") val date: String? = null,
    @param:Json(name = "status") val status: Int? = null,
    @param:Json(name = "statusName") val statusName: String? = null,

    @param:Json(name = "homeTeam") val homeTeam: ApiTeam? = null,
    @param:Json(name = "awayTeam") val awayTeam: ApiTeam? = null,

    @param:Json(name = "season") val season: ApiGameSeason? = null,
    @param:Json(name = "roundName") val roundName: String? = null,

    @param:Json(name = "odds") val odds: Any? = null,
    @param:Json(name = "winner1") val winner1: Double? = null,
    @param:Json(name = "winnerX") val winnerX: Double? = null,
    @param:Json(name = "winner2") val winner2: Double? = null,
)

data class ApiOdds(
    @param:Json(name = "winner1") val winner1: Double? = null,
    @param:Json(name = "winnerX") val winnerX: Double? = null,
    @param:Json(name = "winner2") val winner2: Double? = null,
)

data class ApiGameDetailsData(
    @param:Json(name = "game") val game: ApiGame? = null,
    @param:Json(name = "odds") val odds: Any? = null,
    @param:Json(name = "h2h") val h2h: List<ApiGame>? = null,
)

data class ApiGameGlickoData(
    @param:Json(name = "homeWin") val homeWin: Double? = null,
    @param:Json(name = "draw") val draw: Double? = null,
    @param:Json(name = "awayWin") val awayWin: Double? = null,
)

data class ApiTableRow(
    @param:Json(name = "teamId") val teamId: Int? = null,
    @param:Json(name = "teamName") val teamName: String? = null,
)

data class ApiPlayer(
    @param:Json(name = "id") val id: Long? = null,
    @param:Json(name = "name") val name: String? = null,
    @param:Json(name = "position") val position: String? = null,
    @param:Json(name = "country") val country: ApiCountry? = null,
    
    @param:Json(name = "teamName") val teamName: String? = null,
    
    @param:Json(name = "team") val team: ApiTeam? = null,
)
