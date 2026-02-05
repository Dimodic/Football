package com.example.footballstats.ui.screens.teams

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.footballstats.core.util.DataResult
import com.example.footballstats.data.LeaguesRepository
import com.example.footballstats.data.TeamsRepository
import com.example.footballstats.domain.League
import com.example.footballstats.domain.Team
import kotlinx.coroutines.launch
import java.time.LocalDate

data class TeamsUiState(
    val isLoading: Boolean = true,
    val leagues: List<League> = emptyList(),
    val selectedLeague: League? = null,
    val teams: List<Team> = emptyList(),
    val notice: String? = null,
    val errorMessage: String? = null,
)

class TeamsViewModel(
    private val leaguesRepository: LeaguesRepository,
    private val teamsRepository: TeamsRepository,
) : ViewModel() {

    var uiState by mutableStateOf(TeamsUiState())
        private set

    init {
        reload()
    }

    fun reload() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, notice = null, errorMessage = null)

            when (val leaguesRes = leaguesRepository.getLeagues()) {
                is DataResult.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val leagues = (leaguesRes.data as? List<League>).orEmpty()
                    val selected =
                        uiState.selectedLeague?.let { prev -> leagues.firstOrNull { it.id == prev.id } }
                            ?: leagues.firstOrNull()

                    uiState = uiState.copy(leagues = leagues, selectedLeague = selected)

                    if (selected != null) {
                        loadTeams(selected.id)
                    } else {
                        uiState = uiState.copy(isLoading = false, teams = emptyList())
                    }
                }

                is DataResult.Error<*> -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = leaguesRes.message,
                        leagues = emptyList(),
                        selectedLeague = null,
                        teams = emptyList(),
                    )
                }
            }
        }
    }

    fun onLeagueSelected(league: League) {
        if (uiState.selectedLeague?.id == league.id) return
        uiState = uiState.copy(selectedLeague = league)
        viewModelScope.launch { loadTeams(league.id) }
    }

    private suspend fun loadTeams(leagueId: Int) {
        uiState = uiState.copy(isLoading = true, teams = emptyList(), notice = null, errorMessage = null)

        val year = LocalDate.now().year
        val initial = teamsRepository.getTeamsByLeague(leagueId = leagueId, year = year)
        val result = when (initial) {
            is DataResult.Success<*> -> initial
            is DataResult.Error<*> -> {
                teamsRepository.getTeamsByLeague(leagueId = leagueId, year = year - 1)
            }
        }

        when (result) {
            is DataResult.Success<*> -> {
                @Suppress("UNCHECKED_CAST")
                val teams = (result.data as? List<Team>).orEmpty()
                uiState = uiState.copy(
                    isLoading = false,
                    teams = teams,
                    notice = result.notice,
                    errorMessage = null,
                )
            }

            is DataResult.Error<*> -> {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = result.message,
                    teams = emptyList(),
                )
            }
        }
    }
}
