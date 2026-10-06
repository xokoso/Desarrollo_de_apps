package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppNavegacion()
        }
    }
}

// 1. EL CONTENEDOR PRINCIPAL (Barra inferior de navegación)
@Composable
fun AppNavegacion() {
    val navController = rememberNavController() // El motor que permite navegar
    val rutaActual by navController.currentBackStackEntryAsState() // Dónde estás ahora
    val destinoActual = rutaActual?.destination?.route // Nombre exacto de la ruta

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = destinoActual == "pantalla_pato",
                    onClick = { navController.navigate("pantalla_pato") },
                    icon = { Icon(painterResource(id = R.drawable.arrow), contentDescription = null) },
                    label = { Text("Pato") }
                )

                NavigationBarItem(
                    selected = destinoActual == "pantalla_dos",
                    onClick = { navController.navigate("pantalla_dos") },
                    icon = { Icon(painterResource(id = R.drawable.moon), contentDescription = null) },
                    label = { Text("Pantalla 2") }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "pantalla_pato",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("pantalla_pato") { Pantalla() }
            composable("pantalla_dos") { PantallaDos() }
        }
    }
}

// 2. TU PRIMERA VISTA (Fondo verde y el pato)
@Composable
fun Pantalla() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF92ED1F)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Wenapo dou")
        Text(":c", modifier = Modifier.padding(top = 20.dp))

        Image(
            painter = painterResource(id = R.drawable.images),
            contentDescription = null,
            modifier = Modifier.size(300.dp)
        )

        Text(":c", modifier = Modifier.padding(top = 20.dp))
    }
}

// 3. SEGUNDA PANTALLA CON BARRA SUPERIOR, BOTÓN FLOTANTE Y SCROLL
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDos() {
    // Interruptor para saber si el menú flotante está abierto o cerrado
    var menuExpandido by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Mi Barra Superior") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFEEEEEE)
                )
            )
        },
        floatingActionButton = {
            // Columna para alinear los botones hacia arriba
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Si el menú está abierto, se muestran estos botoncitos
                if (menuExpandido) {
                    SmallFloatingActionButton(
                        onClick = { /* Aquí programaremos qué hace al tocarlo */ },
                        containerColor = Color(0xFFEADDFF)
                    ) {
                        // Ícono de una personita integrado en el sistema
                        Icon(Icons.Filled.Person, contentDescription = "Perfil")
                    }

                    SmallFloatingActionButton(
                        onClick = { /* Aquí programaremos qué hace al tocarlo */ },
                        containerColor = Color(0xFFEADDFF)
                    ) {
                        // Ícono de una casita integrado en el sistema
                        Icon(Icons.Filled.Home, contentDescription = "Inicio")
                    }
                }

                // El botón principal (el morado de hasta abajo)
                FloatingActionButton(
                    onClick = { menuExpandido = !menuExpandido }, // Cambia de falso a verdadero
                    containerColor = Color(0xFFD0BCFF)
                ) {
                    // Cambia visualmente el ícono si está abierto o cerrado
                    val icono = if (menuExpandido) Icons.Filled.Close else Icons.Filled.Add
                    Icon(icono, contentDescription = "Abrir menú")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Esta es la segunda vista", modifier = Modifier.padding(20.dp))

            // Textos extra de relleno para que pruebes deslizar la pantalla hacia abajo
            Text("Intenta tocar el botón flotante de la esquina", modifier = Modifier.padding(40.dp))
            Text("Elemento 3", modifier = Modifier.padding(40.dp))
            Text("Elemento 4", modifier = Modifier.padding(40.dp))
            Text("Elemento 5", modifier = Modifier.padding(40.dp))
            Text("Elemento 6", modifier = Modifier.padding(40.dp))
        }
    }
}