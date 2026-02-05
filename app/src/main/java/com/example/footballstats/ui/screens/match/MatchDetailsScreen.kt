package com.example.footballstats.ui.screens.match

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.footballstats.LocalAppContainer
import com.example.footballstats.core.util.DateTimeUtils
import com.example.footballstats.ui.components.ErrorState
import com.example.footballstats.ui.components.LoadingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailsScreen(
    matchId: Long,
    onBack: () -> Unit
) {
    val container = LocalAppContainer.current

    val vm: MatchDetailsViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return MatchDetailsViewModel(matchId, container.matchesRepository) as T
        }
    })

    val state by vm.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Match Details") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )

        when (val s = state) {
            MatchDetailsUiState.Loading -> LoadingState()
            is MatchDetailsUiState.Error -> ErrorState(message = s.message, onRetry = vm::load)
            is MatchDetailsUiState.Content -> {
                val match = s.match

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(Modifier.height(18.dp))

                    Text(
                        text = match.leagueName,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "${match.homeTeamName} vs ${match.awayTeamName}",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(Modifier.height(6.dp))

                    val dt = match.startTime?.let(DateTimeUtils::formatDetailsDateTime) ?: "—"
                    Text(
                        text = dt,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(Modifier.height(18.dp))

                    Card(shape = MaterialTheme.shapes.large) {
                        Column {
                            KeyValueRow("Country", match.countryName)
                            HorizontalDivider()
                            KeyValueRow("Status", match.statusText, valueColor = MaterialTheme.colorScheme.primary)
                            HorizontalDivider()
                            KeyValueRow("Match ID", match.id.toString())
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    Text(
                        text = "API Endpoints",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(Modifier.height(10.dp))

                    Card(shape = MaterialTheme.shapes.large) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("https://api.sstats.net/games/${match.id}")
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Card(shape = MaterialTheme.shapes.large) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("https://api.sstats.net/games/glicko/${match.id}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyValueRow(
    key: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 16.dp)
    ) {
        Text(
            text = key,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = valueColor
        )
    }
}