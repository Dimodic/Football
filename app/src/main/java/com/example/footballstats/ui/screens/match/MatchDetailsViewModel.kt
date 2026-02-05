package com.example.footballstats.ui.screens.match

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.footballstats.core.util.DataResult
import com.example.footballstats.data.MatchesRepository
import com.example.footballstats.domain.Match
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface MatchDetailsUiState {
    data object Loading : MatchDetailsUiState
    data class Content(val match: Match) : MatchDetailsUiState
    data class Error(val message: String) : MatchDetailsUiState
}

class MatchDetailsViewModel(
    private val matchId: Long,
    private val matchesRepository: MatchesRepository
) : ViewModel() {

    private val _state = MutableStateFlow<MatchDetailsUiState>(MatchDetailsUiState.Loading)
    val state: StateFlow<MatchDetailsUiState> = _state

    init {
        load()
    }

    fun load() {
        _state.value = MatchDetailsUiState.Loading
        viewModelScope.launch {
            when (val res = matchesRepository.getMatchDetails(matchId)) {
                is DataResult.Error -> _state.value = MatchDetailsUiState.Error(res.message)
                is DataResult.Success -> _state.value = MatchDetailsUiState.Content(res.data)
            }
        }
    }
}