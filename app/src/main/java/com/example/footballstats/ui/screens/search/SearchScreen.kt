package com.example.footballstats.ui.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.footballstats.LocalAppContainer
import com.example.footballstats.domain.League
import com.example.footballstats.domain.TimeRange
import com.example.footballstats.ui.components.ErrorState
import com.example.footballstats.ui.components.LoadingState
import com.example.footballstats.ui.components.MatchCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onMatchClick: (Long) -> Unit
) {
    val container = LocalAppContainer.current
    val vm: SearchViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(
                matchesRepository = container.matchesRepository,
                leaguesRepository = container.leaguesRepository
            ) as T
        }
    })

    val state by vm.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(title = { Text("Football Stats") })

        when (val s = state) {
            SearchUiState.Loading -> LoadingState()
            is SearchUiState.Error -> ErrorState(message = s.message, onRetry = vm::load)
            is SearchUiState.Content -> {
                val filters = s.filters

                OutlinedTextField(
                    value = filters.query,
                    onValueChange = vm::updateQuery,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    placeholder = { Text("Search teams, leagues...") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val upcomingSelected = filters.upcoming
                    val pastSelected = !filters.upcoming

                    FilterChip(
                        selected = upcomingSelected,
                        onClick = { vm.updateUpcoming(true) },
                        label = { Text("Upcoming") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = pastSelected,
                        onClick = { vm.updateUpcoming(false) },
                        label = { Text("Archive") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "Filter by League",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(8.dp))

                LeagueDropdown(
                    leagues = s.leagues,
                    selected = filters.selectedLeague,
                    onSelected = vm::updateLeague
                )

                Spacer(Modifier.height(12.dp))

                TimeRangeRow(
                    current = filters.timeRange,
                    onSelect = vm::updateTimeRange
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = if (filters.upcoming) "Upcoming Matches" else "Archive",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(6.dp))

                if (s.matches.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text("No matches found.")
                    }
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(
                            count = s.matches.size,
                            key = { idx -> s.matches[idx].id }
                        ) { idx ->
                            val match = s.matches[idx]
                            MatchCard(match = match, onClick = { onMatchClick(match.id) })
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LeagueDropdown(
    leagues: List<League>,
    selected: League?,
    onSelected: (League?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedText = selected?.name ?: "All Leagues"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .menuAnchor(
                    type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                    enabled = true
                )
                .fillMaxWidth(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("All Leagues") },
                onClick = {
                    onSelected(null)
                    expanded = false
                }
            )
            leagues.forEach { league ->
                val label = buildString {
                    append(league.name)
                    if (!league.countryName.isNullOrBlank()) {
                        append(" • ")
                        append(league.countryName)
                    }
                }
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onSelected(league)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun TimeRangeRow(
    current: TimeRange,
    onSelect: (TimeRange) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FilterChip(
            selected = current == TimeRange.Day,
            onClick = { onSelect(TimeRange.Day) },
            label = { Text("Day") }
        )
        FilterChip(
            selected = current == TimeRange.Week,
            onClick = { onSelect(TimeRange.Week) },
            label = { Text("Week") }
        )
        FilterChip(
            selected = current == TimeRange.Month,
            onClick = { onSelect(TimeRange.Month) },
            label = { Text("Month") }
        )
    }
}