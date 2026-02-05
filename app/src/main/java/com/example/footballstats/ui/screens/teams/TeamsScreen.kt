@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.footballstats.ui.screens.teams

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.footballstats.LocalAppContainer
import com.example.footballstats.domain.League
import com.example.footballstats.domain.Team
import com.example.footballstats.ui.components.EmptyState
import com.example.footballstats.ui.components.ErrorState
import com.example.footballstats.ui.components.LoadingState

@Composable
fun TeamsScreen(
    onTeamClick: (Team) -> Unit,
) {
    val container = LocalAppContainer.current
    val vm: TeamsViewModel = viewModel(
        factory = TeamsViewModelFactory(
            leaguesRepository = container.leaguesRepository,
            teamsRepository = container.teamsRepository,
        )
    )

    val state = vm.uiState
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.notice) {
        state.notice?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Teams") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            LeagueDropdown(
                leagues = state.leagues,
                selected = state.selectedLeague,
                onSelect = vm::onLeagueSelected,
                enabled = !state.isLoading && state.leagues.isNotEmpty(),
            )

            Spacer(Modifier.height(12.dp))

            when {
                state.isLoading -> LoadingState(Modifier.fillMaxSize())

                state.errorMessage != null -> ErrorState(
                    message = state.errorMessage,
                    onRetry = vm::reload,
                    modifier = Modifier.fillMaxSize(),
                )

                state.teams.isEmpty() -> EmptyState(
                    title = "No teams",
                    subtitle = "No teams found for this league.",
                    modifier = Modifier.fillMaxSize(),
                )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.teams, key = { it.id }) { team ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTeamClick(team) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = team.name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeagueDropdown(
    leagues: List<League>,
    selected: League?,
    onSelect: (League) -> Unit,
    enabled: Boolean,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            if (enabled) expanded = !expanded
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = selected?.name ?: "",
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            singleLine = true,
            label = { Text("Filter by League") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = enabled),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            leagues.forEach { league ->
                DropdownMenuItem(
                    text = { Text(league.name) },
                    onClick = {
                        expanded = false
                        onSelect(league)
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

private class TeamsViewModelFactory(
    private val leaguesRepository: com.example.footballstats.data.LeaguesRepository,
    private val teamsRepository: com.example.footballstats.data.TeamsRepository,
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return TeamsViewModel(leaguesRepository, teamsRepository) as T
    }
}