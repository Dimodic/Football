package com.example.footballstats.ui.navigation

import android.net.Uri

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.footballstats.ui.screens.account.AccountScreen
import com.example.footballstats.ui.screens.home.HomeScreen
import com.example.footballstats.ui.screens.match.MatchDetailsScreen
import com.example.footballstats.ui.screens.search.SearchScreen
import com.example.footballstats.ui.screens.teams.PlayersScreen
import com.example.footballstats.ui.screens.teams.TeamsScreen

sealed class TopDest(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Home : TopDest("home", "Home", Icons.AutoMirrored.Outlined.TrendingUp)
    data object Search : TopDest("search", "Search", Icons.Outlined.Search)
    data object Teams : TopDest("teams", "Teams", Icons.Outlined.Groups)
    data object Account : TopDest("account", "Account", Icons.Outlined.PersonOutline)
}

private val topLevel = listOf(TopDest.Home, TopDest.Search, TopDest.Teams, TopDest.Account)

@Composable
fun AppNavRoot() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = topLevel.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomBar(navController = navController)
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = TopDest.Home.route
        ) {
            composable(TopDest.Home.route) {
                HomeScreen(
                    onMatchClick = { matchId -> navController.navigate("match/$matchId") }
                )
            }
            composable(TopDest.Search.route) {
                SearchScreen(
                    onMatchClick = { matchId -> navController.navigate("match/$matchId") }
                )
            }
            composable(TopDest.Account.route) {
                AccountScreen()
            }
            composable(TopDest.Teams.route) {
                TeamsScreen(
                    onTeamClick = { team ->
                        navController.navigate("players/${team.id}/${Uri.encode(team.name)}")
                    }
                )
            }
            composable("match/{id}") { entry ->
                val id = entry.arguments?.getString("id")?.toLongOrNull() ?: -1L
                MatchDetailsScreen(
                    matchId = id,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("players/{teamId}/{teamName}") { entry ->
                val teamId = entry.arguments?.getString("teamId")?.toIntOrNull() ?: -1
                val teamName = Uri.decode(entry.arguments?.getString("teamName") ?: "")
                PlayersScreen(
                    teamId = teamId,
                    teamName = teamName,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun BottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        topLevel.forEach { dest ->
            val selected = currentDestination?.route == dest.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(dest.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(dest.icon, contentDescription = dest.label) },
                label = { Text(dest.label) }
            )
        }
    }
}