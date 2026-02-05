package com.example.footballstats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.footballstats.ui.navigation.AppNavRoot
import com.example.footballstats.ui.theme.FootballStatsTheme

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer not provided")
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = (application as FootballStatsApp).container

        setContent {
            FootballStatsTheme {
                CompositionLocalProvider(LocalAppContainer provides container) {
                    AppNavRoot()
                }
            }
        }
    }
}