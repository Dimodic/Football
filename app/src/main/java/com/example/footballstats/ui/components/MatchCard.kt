package com.example.footballstats.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.example.footballstats.core.util.DateTimeUtils
import com.example.footballstats.domain.Match

@Composable
fun MatchCard(
    match: Match,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = flagEmoji(match.countryName),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = match.countryName,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.weight(1f))

                val chipText = match.startTime?.let { DateTimeUtils.chipLabel(it) } ?: "—"
                AssistChip(
                    onClick = {},
                    label = { Text(chipText) },
                    enabled = false
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    TeamAvatar(name = match.homeTeamName)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = match.homeTeamName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.Schedule, contentDescription = null)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = match.startTime?.let(DateTimeUtils::formatTime) ?: "--:--",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = match.awayTeamName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(8.dp))
                    TeamAvatar(name = match.awayTeamName)
                }
            }

            if (match.odds != null) {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "1: ${match.odds.win1 ?: "-"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "X: ${match.odds.drawX ?: "-"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "2: ${match.odds.win2 ?: "-"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider()
            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                LeagueAvatar(name = match.leagueName)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = match.leagueName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Outlined.ShowChart, contentDescription = null)
            }
        }
    }
}

@Composable
private fun TeamAvatar(name: String, size: Dp = 26.dp) {
    Surface(
        modifier = Modifier.size(size),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = initials(name),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun LeagueAvatar(name: String, size: Dp = 22.dp) {
    Surface(
        modifier = Modifier.size(size),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Outlined.EmojiEvents,
                contentDescription = null,
                modifier = Modifier.size(size * 0.65f),
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

private fun initials(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts.first().take(2).uppercase()
        else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
    }
}

private fun flagEmoji(countryName: String): String {
    
    return when (countryName.trim().lowercase()) {
        "england", "united kingdom", "great britain" -> "🇬🇧"
        "spain" -> "🇪🇸"
        "italy" -> "🇮🇹"
        "germany" -> "🇩🇪"
        "australia" -> "🇦🇺"
        "japan" -> "🇯🇵"
        "hong-kong", "hong kong" -> "🇭🇰"
        else -> "🏳️"
    }
}