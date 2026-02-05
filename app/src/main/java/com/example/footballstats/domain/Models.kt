package com.example.footballstats.domain

import java.time.Instant

data class AppConfig(
    val baseUrl: String,
    val apiKey: String
)

data class League(
    val id: Int,
    val name: String,
    val countryName: String?
)

data class Odds1X2(
    val win1: Double?,
    val drawX: Double?,
    val win2: Double?
)

enum class MatchStatus {
    Upcoming,
    Past,
    Live,
    Unknown
}

data class Match(
    val id: Long,
    val countryName: String,
    val leagueName: String,
    val homeTeamName: String,
    val awayTeamName: String,
    val startTime: Instant?,
    val statusText: String,
    val odds: Odds1X2? = null
)

enum class TimeRange(val days: Long) {
    Day(1),
    Week(7),
    Month(30)
}

data class Team(
    val id: Int,
    val name: String,
)

data class Player(
    val id: Long? = null,
    val name: String,
    val position: String? = null,
    val countryName: String? = null,
)