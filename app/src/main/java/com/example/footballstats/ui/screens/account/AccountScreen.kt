package com.example.footballstats.ui.screens.account

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.footballstats.LocalAppContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen() {
    val container = LocalAppContainer.current
    val vm: AccountViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AccountViewModel(container.authRepository) as T
        }
    })

    val isLoggedIn by vm.isLoggedIn.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(title = { Text("Account") })

        if (!isLoggedIn) {
            LoginForm(onLogin = vm::login)
        } else {
            LoggedInState(onLogout = vm::logout)
        }
    }
}

@Composable
private fun LoginForm(
    onLogin: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Sign in / Register", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            singleLine = true
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { onLogin(email, pass) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue")
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Note: per task requirements, login always succeeds.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun LoggedInState(onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("You are logged in.", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onLogout) {
            Text("Logout")
        }
    }
}