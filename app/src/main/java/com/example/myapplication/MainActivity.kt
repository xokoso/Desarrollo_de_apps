package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// 1. Representación de pantallas y navegación
enum class Screen(
    val label: String,
    val icon: ImageVector
) {
    Home("Inicio", Icons.Default.Home),
    Alerts("Alertas", Icons.Default.Warning),
    Quarters("Cuarteles", Icons.Default.List),
    History("Historial", Icons.Default.History),
    Profile("Perfil", Icons.Default.Person),
    Detail("Detalle", Icons.Default.List) // Sin pestaña propia en la barra inferior
}

// Color de fondo general
private val Background = Color(0xFFF5F5F5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                FrutisMonitorApp()
            }
        }
    }
}

@Composable
private fun FrutisMonitorApp() {
    var screen by remember { mutableStateOf(Screen.Home) }

    val selectedNavigation =
        if (screen == Screen.Detail) Screen.Quarters else screen

    Scaffold(
        containerColor = Background,
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                Screen.entries
                    .filter { it != Screen.Detail }
                    .forEach { destination ->
                        NavigationBarItem(
                            selected = selectedNavigation == destination,
                            onClick = { screen = destination },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.label
                                )
                            },
                            label = {
                                Text(destination.label)
                            }
                        )
                    }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (screen) {
                Screen.Home -> HomeScreen(
                    openAlerts = { screen = Screen.Alerts },
                    openQuarters = { screen = Screen.Quarters },
                    openDetail = { screen = Screen.Detail }
                )

                Screen.Alerts -> AlertsScreen()

                Screen.Quarters -> QuartersScreen(
                    onSelectQuarter = { screen = Screen.Detail }
                )

                Screen.Detail -> QuarterDetailScreen(
                    onBack = { screen = Screen.Quarters }
                )

                Screen.History -> HistoryScreen()

                Screen.Profile -> ProfileScreen()
            }
        }
    }
}

// 2. Componentes de Vistas (Placeholders)

@Composable
private fun HomeScreen(
    openAlerts: () -> Unit,
    openQuarters: () -> Unit,
    openDetail: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Pantalla de Inicio")
    }
}

@Composable
private fun AlertsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Pantalla de Alertas")
    }
}

@Composable
private fun QuartersScreen(onSelectQuarter: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Pantalla de Cuarteles")
    }
}

@Composable
private fun QuarterDetailScreen(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Detalle del Cuartel")
    }
}

@Composable
private fun HistoryScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Pantalla de Historial")
    }
}

@Composable
private fun ProfileScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Pantalla de Perfil")
    }
}
