package com.example.footballstats.ui.screens.teams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.footballstats.core.util.DataResult
import com.example.footballstats.data.TeamsRepository
import com.example.footballstats.domain.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PlayersUiState {
    data object Loading : PlayersUiState
    data class Content(val players: List<Player>) : PlayersUiState
    data class Error(val message: String) : PlayersUiState
}

class PlayersViewModel(
    private val teamsRepository: TeamsRepository,
    private val teamName: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlayersUiState>(PlayersUiState.Loading)
    val uiState: StateFlow<PlayersUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = PlayersUiState.Loading
        viewModelScope.launch {
            when (val res = teamsRepository.getPlayersByTeamName(teamName)) {
                is DataResult.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val players = (res.data as? List<Player>).orEmpty()
                    _uiState.value = PlayersUiState.Content(players)
                }
                is DataResult.Error<*> -> _uiState.value = PlayersUiState.Error(res.message)
            }
        }
    }

    companion object {
        fun factory(repo: TeamsRepository, teamName: String) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PlayersViewModel(repo, teamName) as T
            }
        }
    }
}
