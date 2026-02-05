package com.example.footballstats.ui.screens.teams

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.footballstats.LocalAppContainer
import com.example.footballstats.domain.Player

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayersScreen(
    teamId: Int,
    teamName: String,
    onBack: () -> Unit
) {
    val container = LocalAppContainer.current
    val vm: PlayersViewModel = viewModel(
        factory = PlayersViewModel.factory(container.teamsRepository, teamName)
    )
    val state by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = teamName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (state) {
                is PlayersUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is PlayersUiState.Error -> {
                    val msg = (state as PlayersUiState.Error).message
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(msg, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { vm.load() }) { Text("Retry") }
                    }
                }
                is PlayersUiState.Content -> {
                    val players = (state as PlayersUiState.Content).players
                    if (players.isEmpty()) {
                        Text(
                            "No players found.",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        PlayersList(players = players)
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayersList(players: List<Player>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(players) { p ->
            PlayerRow(player = p)
        }
    }
}

@Composable
private fun PlayerRow(player: Player) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(player.name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val position = player.position
                if (!position.isNullOrBlank()) {
                    AssistChip(onClick = {}, label = { Text(position) })
                }

                val countryName = player.countryName
                if (!countryName.isNullOrBlank()) {
                    AssistChip(onClick = {}, label = { Text(countryName) })
                }
            }
        }
    }
}

