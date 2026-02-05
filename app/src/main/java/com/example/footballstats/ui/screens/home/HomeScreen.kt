package com.example.footballstats.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.footballstats.LocalAppContainer
import com.example.footballstats.ui.components.EmptyState
import com.example.footballstats.ui.components.ErrorState
import com.example.footballstats.ui.components.LoadingState
import com.example.footballstats.ui.components.MatchCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onMatchClick: (Long) -> Unit
) {
    val container = LocalAppContainer.current
    val vm: HomeViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(container.matchesRepository) as T
        }
    })

    val state by vm.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {

        CenterAlignedTopAppBar(
            title = { Text("Football Stats") }
        )

        when (val s = state) {
            HomeUiState.Loading -> LoadingState()
            HomeUiState.Empty -> EmptyState(
                title = "No matches",
                subtitle = "No upcoming matches found for the next 2 days."
            )
            is HomeUiState.Error -> ErrorState(
                message = s.message,
                onRetry = vm::refresh
            )
            is HomeUiState.Content -> {
                if (s.fromCache || !s.notice.isNullOrBlank()) {
                    Surface(
                        tonalElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = s.notice ?: "Showing cached data (network issue).",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Row(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Outlined.TrendingUp, contentDescription = null)
                    Text(
                        text = s.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(
                        count = s.matches.size,
                        key = { idx -> s.matches[idx].id }
                    ) { idx ->
                        val match = s.matches[idx]
                        MatchCard(
                            match = match,
                            onClick = { onMatchClick(match.id) }
                        )
                    }
                }
            }
        }
    }
}