package com.example.footballstats.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.footballstats.core.util.DataResult
import com.example.footballstats.data.LeaguesRepository
import com.example.footballstats.data.MatchesRepository
import com.example.footballstats.domain.League
import com.example.footballstats.domain.Match
import com.example.footballstats.domain.TimeRange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SearchFilters(
    val query: String = "",
    val selectedLeague: League? = null,
    val timeRange: TimeRange = TimeRange.Month,
    val upcoming: Boolean = true
)

sealed interface SearchUiState {
    data object Loading : SearchUiState
    data class Content(
        val leagues: List<League>,
        val filters: SearchFilters,
        val matches: List<Match>
    ) : SearchUiState

    data class Error(val message: String) : SearchUiState
}

class SearchViewModel(
    private val matchesRepository: MatchesRepository,
    private val leaguesRepository: LeaguesRepository
) : ViewModel() {

    private val _state = MutableStateFlow<SearchUiState>(SearchUiState.Loading)
    val state: StateFlow<SearchUiState> = _state

    private var leaguesCache: List<League> = emptyList()
    private var filters: SearchFilters = SearchFilters()

    init {
        load()
    }

    fun load() {
        _state.value = SearchUiState.Loading
        viewModelScope.launch {
            val leaguesRes = leaguesRepository.getLeagues()
            when (leaguesRes) {
                is DataResult.Error -> {
                    _state.value = SearchUiState.Error(leaguesRes.message)
                }
                is DataResult.Success -> {
                    leaguesCache = leaguesRes.data
                    refreshMatches()
                }
            }
        }
    }

    fun updateQuery(q: String) {
        filters = filters.copy(query = q)
        refreshMatches()
    }

    fun updateLeague(league: League?) {
        filters = filters.copy(selectedLeague = league)
        refreshMatches()
    }

    fun updateUpcoming(upcoming: Boolean) {
        filters = filters.copy(upcoming = upcoming)
        refreshMatches()
    }

    fun updateTimeRange(range: TimeRange) {
        filters = filters.copy(timeRange = range)
        refreshMatches()
    }

    private fun refreshMatches() {
        _state.value = SearchUiState.Loading
        viewModelScope.launch {
            val res = matchesRepository.searchMatches(
                leagueId = filters.selectedLeague?.id,
                rangeDays = filters.timeRange.days.toInt(),
                upcoming = filters.upcoming,
                query = filters.query
            )
            when (res) {
                is DataResult.Error -> _state.value = SearchUiState.Error(res.message)
                is DataResult.Success -> _state.value = SearchUiState.Content(
                    leagues = leaguesCache,
                    filters = filters,
                    matches = res.data
                )
            }
        }
    }
}