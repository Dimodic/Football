package com.example.footballstats.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.footballstats.core.util.DataResult
import com.example.footballstats.data.MatchesRepository
import com.example.footballstats.domain.Match
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Content(
        val matches: List<Match>,
        val fromCache: Boolean,
        val notice: String? = null,
        val title: String = "Popular Upcoming Matches",
    ) : HomeUiState
    data object Empty : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel(
    private val matchesRepository: MatchesRepository
) : ViewModel() {

    private val _state = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val state: StateFlow<HomeUiState> = _state

    init {
        refresh()
    }

    fun refresh() {
        _state.value = HomeUiState.Loading
        viewModelScope.launch {
            when (val res = matchesRepository.getUpcomingForNextDays(days = 2)) {
                is DataResult.Success -> {
                    val data = res.data
                    _state.value = if (data.isEmpty()) HomeUiState.Empty
                    else {
                        val title = if (res.isFromCache && (res.notice?.contains("finished", ignoreCase = true) == true)) {
                            "Finished Matches"
                        } else {
                            "Popular Upcoming Matches"
                        }
                        HomeUiState.Content(
                            matches = data,
                            fromCache = res.isFromCache,
                            notice = res.notice,
                            title = title,
                        )
                    }
                }
                is DataResult.Error -> {
                    _state.value = HomeUiState.Error(res.message)
                }
            }
        }
    }
}