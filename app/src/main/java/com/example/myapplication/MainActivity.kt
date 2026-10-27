package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- PALETA DE COLORES ---
val PrimaryGreen = Color(0xFF1E5642)
val LightGreenBg = Color(0xFFEBF3EF)
val ScreenBackground = Color(0xFFF6F8F7)
val CardBackground = Color.White
val TextPrimary = Color(0xFF1A1A1A)
val TextSecondary = Color(0xFF666666)
val CriticalRed = Color(0xFFD9534F)
val CriticalRedBg = Color(0xFFFDF0ED)
val WarningAmber = Color(0xFFE67E22)
val WarningAmberBg = Color(0xFFFEF5ED)

// --- MODELOS DE DATOS ---
data class AlertItemData(
    val id: String,
    val title: String,
    val value: String,
    val level: String,
    val levelColor: Color,
    val levelBg: Color,
    val location: String,
    val time: String,
    val icon: ImageVector,
    val assignedAreaIncharge: String = "Juan Pérez (Operaciones Riego)",
    var isResolved: Boolean = false
)

data class HistoryItemData(
    val id: String,
    val time: String,
    val title: String,
    val location: String,
    val dotColor: Color,
    val category: String,
    val isOlder: Boolean = false,
    val details: String = "Telemetría normal registrada por sensores agronómicos."
)

data class DeviceStatus(
    val id: String,
    val name: String,
    val type: String,
    val isConnected: Boolean,
    val battery: String
)

// --- ENUM DE NAVEGACIÓN ---
enum class Screen(
    val label: String,
    val icon: ImageVector,
    val isAdminOnly: Boolean = false
) {
    Home("Inicio", Icons.Default.Home),
    Alerts("Alertas", Icons.Default.Notifications),
    Quarters("Cuarteles", Icons.AutoMirrored.Filled.List),
    AdminAlerts("Gestión Alertas", Icons.Default.AdminPanelSettings, isAdminOnly = true),
    Analytics("Métricas", Icons.Default.BarChart, isAdminOnly = true),
    History("Historial", Icons.Default.History),
    Profile("Perfil", Icons.Default.Person),
    Detail("Detalle", Icons.Default.Info),
    Login("Login", Icons.Default.Lock)
}

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

    // Sesión y Usuarios
    var isLoggedIn by remember { mutableStateOf(true) }
    var currentUserEmail by remember { mutableStateOf("carlos.munoz@asti.cl") }
    var currentUserName by remember { mutableStateOf("Carlos") }
    var isAdmin by remember { mutableStateOf(false) }

    var isRiegoAntiHeladaActive by remember { mutableStateOf(false) }

    val alertsList = remember {
        mutableStateListOf(
            AlertItemData("1", "Riesgo de helada", "2,1 °C", "Crítica", CriticalRed, CriticalRedBg, "Cuartel Norte · Estación T-01", "Hoy, 06:42", Icons.Default.AcUnit, "Carlos Muñoz (Supervisor)"),
            AlertItemData("2", "Caudal irregular", "-32%", "Media", WarningAmber, WarningAmberBg, "Cuartel 04 · Línea secundaria", "Hoy, 06:26", Icons.Default.WaterDrop, "Mario Soto (Encargado Riego)"),
            AlertItemData("3", "Nivel de estanque bajo", "19%", "Media", WarningAmber, WarningAmberBg, "Estanque principal", "Ayer, 18:14", Icons.Default.FormatColorFill, "Roberto Díaz (Infraestructura)", isResolved = true)
        )
    }

    val historyList = remember {
        mutableStateListOf(
            HistoryItemData("1", "Hoy · 06:42", "Riesgo de helada detectado", "Cuartel Norte", CriticalRed, "Alertas", details = "Temperatura bajó a 2,1°C en Estación T-01. Riesgo de congelamiento de yemas."),
            HistoryItemData("2", "Hoy · 06:26", "Caudal bajo umbral", "Cuartel 04", WarningAmber, "Alertas", details = "Flujo de agua redujo en 32% en la línea secundaria por posible fisura."),
            HistoryItemData("3", "Hoy · 06:15", "Riego iniciado", "Cuartel Norte", PrimaryGreen, "Riego", details = "Ciclo programado de humidificación previo a salida del sol."),
            HistoryItemData("4", "Ayer · 18:42", "Riego finalizado", "Cuartel 03", PrimaryGreen, "Riego", details = "Finalizado sin anomalías. Consumo total: 12.400 Litros."),
            HistoryItemData("5", "Ayer · 18:14", "Nivel de estanque bajo", "Estanque principal", WarningAmber, "Alertas", details = "El estanque bajó del 20% mínimo configurado."),
            HistoryItemData("6", "Ayer · 17:04", "Bomba reiniciada por técnico", "Sistema central", Color(0xFF2980B9), "Sistema", details = "Mantenimiento rutinario de bomba trifásica completado.")
        )
    }

    val devicesList = remember {
        mutableStateListOf(
            DeviceStatus("1", "Estación T-01", "Temperatura y Humedad", true, "95%"),
            DeviceStatus("2", "Estación T-02", "Temperatura", true, "88%"),
            DeviceStatus("3", "Sensor Suelo S-01", "Humedad Suelo", true, "91%"),
            DeviceStatus("4", "Sensor Suelo S-02", "Humedad Suelo", true, "84%"),
            DeviceStatus("5", "Caudalímetro C-01", "Caudal de Agua", true, "100%"),
            DeviceStatus("6", "Caudalímetro C-02", "Caudal de Agua", false, "0%"),
            DeviceStatus("7", "Sensor Estanque E-01", "Nivel de Agua", true, "79%"),
            DeviceStatus("8", "Piranómetro R-01", "Radiación Solar", true, "92%"),
            DeviceStatus("9", "Válvula V-01", "Control Riego", true, "100%"),
            DeviceStatus("10", "Gateway LoRa Buin", "Conectividad Base", true, "100%")
        )
    }

    val activeAlertsCount = alertsList.count { !it.isResolved }

    if (!isLoggedIn) {
        LoginScreen(onLoginSuccess = { email ->
            currentUserEmail = email
            val rawName = email.substringBefore("@")
            currentUserName = rawName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            isAdmin = email.contains("@admin.com", ignoreCase = true)
            isLoggedIn = true
            screen = Screen.Home
        })
        return
    }

    val selectedNavigation = if (screen == Screen.Detail) Screen.Quarters else screen

    Scaffold(
        containerColor = ScreenBackground,
        bottomBar = {
            if (screen != Screen.Login) {
                NavigationBar(containerColor = Color.White) {
                    Screen.entries
                        .filter { it != Screen.Detail && it != Screen.Login }
                        .filter { !it.isAdminOnly || isAdmin }
                        .forEach { destination ->
                            NavigationBarItem(
                                selected = selectedNavigation == destination,
                                onClick = { screen = destination },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (destination == Screen.Alerts && activeAlertsCount > 0) {
                                                Badge(
                                                    containerColor = CriticalRed,
                                                    contentColor = Color.White
                                                ) {
                                                    Text("$activeAlertsCount")
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = destination.icon,
                                            contentDescription = destination.label,
                                            tint = if (selectedNavigation == destination) PrimaryGreen else Color.Gray
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        destination.label,
                                        color = if (selectedNavigation == destination) PrimaryGreen else Color.Gray,
                                        fontSize = 10.sp
                                    )
                                }
                            )
                        }
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
                    userName = currentUserName,
                    alertsList = alertsList,
                    devicesList = devicesList,
                    isRiegoActive = isRiegoAntiHeladaActive,
                    openAlerts = { screen = Screen.Alerts },
                    openQuarters = { screen = Screen.Quarters },
                    openDetail = { screen = Screen.Detail }
                )
                Screen.Alerts -> AlertsScreen(
                    alertsList = alertsList,
                    openDetail = { screen = Screen.Detail }
                )
                Screen.Quarters -> QuartersScreen(
                    isRiegoActive = isRiegoAntiHeladaActive,
                    onSelectQuarter = { screen = Screen.Detail }
                )
                Screen.AdminAlerts -> AdminAlertsScreen(
                    alertsList = alertsList,
                    openDetail = { screen = Screen.Detail }
                )
                Screen.Analytics -> AnalyticsAdminScreen()
                Screen.Detail -> QuarterDetailScreen(
                    isRiegoActive = isRiegoAntiHeladaActive,
                    onToggleRiego = { active ->
                        isRiegoAntiHeladaActive = active
                        val title = if (active) "Riego anti-helada ACTIVADO" else "Riego anti-helada DETENIDO"
                        historyList.add(
                            0,
                            HistoryItemData(
                                id = System.currentTimeMillis().toString(),
                                time = "Hoy · Justo ahora",
                                title = title,
                                location = "Cuartel Norte",
                                dotColor = if (active) PrimaryGreen else CriticalRed,
                                category = "Riego",
                                details = if (active) "Comando remoto ejecutado por $currentUserName." else "Asperjadores detenidos manualmente."
                            )
                        )
                    },
                    onBack = { screen = Screen.Quarters }
                )
                Screen.History -> HistoryScreen(historyList = historyList)
                Screen.Profile -> ProfileScreen(
                    userName = currentUserName,
                    userEmail = currentUserEmail,
                    isAdmin = isAdmin,
                    onLogout = { isLoggedIn = false }
                )
                Screen.Login -> LoginScreen(onLoginSuccess = { email ->
                    currentUserEmail = email
                    val rawName = email.substringBefore("@")
                    currentUserName = rawName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    isAdmin = email.contains("@admin.com", ignoreCase = true)
                    isLoggedIn = true
                    screen = Screen.Home
                })
            }
        }
    }
}

// ==========================================
// 0. PANTALLA DE LOGIN DINÁMICA
// ==========================================
@Composable
private fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(PrimaryGreen),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.BarChart, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("FRUTIS MONITOR", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
        Text("Ingrese sus credenciales para continuar", fontSize = 13.sp, color = TextSecondary)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            placeholder = { Text("ejemplo@admin.com") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    val roleText = if (email.contains("@admin.com", ignoreCase = true)) " [Modo Administrador]" else ""
                    Toast.makeText(context, "Bienvenido a Frutis Monitor$roleText", Toast.LENGTH_SHORT).show()
                    onLoginSuccess(email)
                } else {
                    Toast.makeText(context, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Iniciar sesión", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// 1. PANTALLA INICIO (HOME)
// ==========================================
@Composable
private fun HomeScreen(
    userName: String,
    alertsList: List<AlertItemData>,
    devicesList: MutableList<DeviceStatus>,
    isRiegoActive: Boolean,
    openAlerts: () -> Unit,
    openQuarters: () -> Unit,
    openDetail: () -> Unit
) {
    val context = LocalContext.current
    var showDevicesDialog by remember { mutableStateOf(false) }

    val activeCount = alertsList.count { !it.isResolved }
    val firstActiveAlert = alertsList.firstOrNull { !it.isResolved }
    val connectedDevices = devicesList.count { it.isConnected }

    if (showDevicesDialog) {
        SystemStatusDialog(
            devicesList = devicesList,
            onDismiss = { showDevicesDialog = false },
            onReconnectDevice = { dev ->
                val idx = devicesList.indexOfFirst { it.id == dev.id }
                if (idx != -1) {
                    devicesList[idx] = devicesList[idx].copy(isConnected = true, battery = "100%")
                    Toast.makeText(context, "${dev.name} reconectado", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryGreen)
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BarChart, contentDescription = null, tint = Color(0xFF6EE7B7), modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("FRUTIS MONITOR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        IconButton(
                            onClick = openAlerts,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            BadgedBox(badge = {
                                if (activeCount > 0) Badge(containerColor = CriticalRed) { Text("$activeCount") }
                            }) {
                                Icon(Icons.Outlined.Notifications, contentDescription = "Alertas", tint = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Buenos días, $userName", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Predio Buin · Última lectura hace 2 min", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)

                    if (isRiegoActive) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF2E7D32))
                                .clickable { openDetail() }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Riego Anti-Helada ACTIVO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Cuartel Norte · Aspersión en curso", color = Color.White.copy(0.8f), fontSize = 10.sp)
                                }
                            }
                            Text("VER", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-20).dp)
                        .clickable { showDevicesDialog = true }
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(if (connectedDevices == devicesList.size) Color(0xFF2ECC71) else WarningAmber))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Sistema operativo", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("$connectedDevices de ${devicesList.size} dispositivos conectados", fontSize = 12.sp, color = TextSecondary)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Resumen del predio", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(LightGreenBg).padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Text("● Estable", color = PrimaryGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IndicatorCard(modifier = Modifier.weight(1f), icon = Icons.Default.WaterDrop, iconBg = LightGreenBg, iconTint = PrimaryGreen, label = "Humedad suelo", value = "67%", subtext = "Rango óptimo")
                    IndicatorCard(modifier = Modifier.weight(1f), icon = Icons.Default.AcUnit, iconBg = Color(0xFFEBF5FF), iconTint = Color(0xFF2980B9), label = "Temperatura", value = "3,2°", subtext = "Descendiendo")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IndicatorCard(modifier = Modifier.weight(1f), icon = Icons.Default.FormatColorFill, iconBg = LightGreenBg, iconTint = PrimaryGreen, label = "Estanque", value = "78%", subtext = "124.800 L")
                    IndicatorCard(modifier = Modifier.weight(1f), icon = Icons.Default.WbSunny, iconBg = Color(0xFFFEF9E7), iconTint = Color(0xFFF39C12), label = "Radiación", value = "182", subtext = "W/m²")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Atención requerida", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    Text("Ver todas", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.clickable { openAlerts() })
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (firstActiveAlert != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth().clickable { openDetail() }
                    ) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(firstActiveAlert.levelBg), contentAlignment = Alignment.Center) {
                                Icon(firstActiveAlert.icon, contentDescription = null, tint = firstActiveAlert.levelColor)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(firstActiveAlert.title, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(firstActiveAlert.levelBg).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                        Text(firstActiveAlert.level.uppercase(), color = firstActiveAlert.levelColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(firstActiveAlert.location, fontSize = 12.sp, color = TextSecondary)
                                Text(firstActiveAlert.time, fontSize = 11.sp, color = TextSecondary)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No hay alertas pendientes 🎉", color = PrimaryGreen, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// PANTALLA EXCLUSIVA DE ADMIN: GESTIÓN DE ALERTAS CON ENCARGADO
// ==========================================
@Composable
private fun AdminAlertsScreen(
    alertsList: MutableList<AlertItemData>,
    openDetail: () -> Unit
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        TopBarHeader(title = "Gestión Global Alertas", subtitle = "PANEL ADMINISTRADOR")

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Supervisión y Asignación de Encargados",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(alertsList, key = { it.id }) { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(item.levelBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(item.icon, contentDescription = null, tint = item.levelColor)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(item.location, fontSize = 11.sp, color = TextSecondary)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(item.levelBg)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(item.level, color = item.levelColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Encargado del área: ", fontSize = 12.sp, color = TextSecondary)
                            Text(item.assignedAreaIncharge, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (!item.isResolved) {
                            Button(
                                onClick = {
                                    val index = alertsList.indexOfFirst { it.id == item.id }
                                    if (index != -1) {
                                        alertsList[index] = alertsList[index].copy(isResolved = true)
                                        Toast.makeText(context, "Alerta resuelta por Administrador", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Resolver Alerta como Admin", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(LightGreenBg)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✅ RESUELTA Y AUDITADA POR ADMIN", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// PANTALLA EXCLUSIVA DE ADMIN: GRÁFICOS Y MÉTRICAS
// ==========================================
@Composable
private fun AnalyticsAdminScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        TopBarHeader(title = "Analítica Agronómica", subtitle = "PANEL ADMINISTRADOR")

        LazyColumn(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Indicadores Críticos del Predio", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Consumo Hídrico Semanal (L/ha)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Promedio: 14.200 L/ha · Eficiencia 94%", fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(16.dp))

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            val bars = listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.85f, 0.3f, 0.6f)
                            val barWidth = size.width / (bars.size * 2)

                            bars.forEachIndexed { idx, ratio ->
                                val x = idx * barWidth * 2 + barWidth / 2
                                val h = size.height * ratio
                                drawRect(
                                    color = PrimaryGreen,
                                    topLeft = Offset(x, size.height - h),
                                    size = androidx.compose.ui.geometry.Size(barWidth, h)
                                )
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom").forEach {
                                Text(it, fontSize = 10.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Curva de Temperatura Crítica - Helada", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Registrado hoy de 00:00 a 07:00 hrs", fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(16.dp))

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                        ) {
                            val points = listOf(
                                Offset(0f, size.height * 0.2f),
                                Offset(size.width * 0.25f, size.height * 0.4f),
                                Offset(size.width * 0.5f, size.height * 0.85f),
                                Offset(size.width * 0.75f, size.height * 0.6f),
                                Offset(size.width, size.height * 0.3f)
                            )
                            for (i in 0 until points.size - 1) {
                                drawLine(
                                    color = CriticalRed,
                                    start = points[i],
                                    end = points[i + 1],
                                    strokeWidth = 5f
                                )
                            }
                        }
                        Text("⚠️ Caída bajo los 2,5°C registrada a las 06:15", fontSize = 10.sp, color = CriticalRed, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Diálogo de Estado del Sistema
@Composable
private fun SystemStatusDialog(
    devicesList: List<DeviceStatus>,
    onDismiss: () -> Unit,
    onReconnectDevice: (DeviceStatus) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Estado de Dispositivos", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(devicesList) { dev ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ScreenBackground),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (dev.isConnected) Color(0xFF2ECC71) else CriticalRed)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(dev.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(dev.type, fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                            if (dev.isConnected) {
                                Text("🔋 ${dev.battery}", fontSize = 11.sp, color = PrimaryGreen, fontWeight = FontWeight.Medium)
                            } else {
                                TextButton(
                                    onClick = { onReconnectDevice(dev) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                ) {
                                    Text("Reconectar", color = CriticalRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)) {
                Text("Cerrar", color = Color.White)
            }
        }
    )
}

@Composable
private fun IndicatorCard(modifier: Modifier = Modifier, icon: ImageVector, iconBg: Color, iconTint: Color, label: String, value: String, subtext: String) {
    Card(colors = CardDefaults.cardColors(containerColor = CardBackground), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(1.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(iconBg), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(label, fontSize = 12.sp, color = TextSecondary)
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(subtext, fontSize = 11.sp, color = TextSecondary)
        }
    }
}

// ==========================================
// 2. PANTALLA ALERTAS USUARIO GENERAL
// ==========================================
@Composable
private fun AlertsScreen(
    alertsList: MutableList<AlertItemData>,
    openDetail: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    val filteredList = alertsList.filter {
        if (selectedTab == 0) !it.isResolved else it.isResolved
    }

    val activeCount = alertsList.count { !it.isResolved }
    val resolvedCount = alertsList.count { it.isResolved }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBarHeader(title = "Alertas", subtitle = "CENTRO DE EVENTOS")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE2E8F0))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedTab == 0) Color.White else Color.Transparent)
                    .clickable { selectedTab = 0 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Activas  $activeCount", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (selectedTab == 0) PrimaryGreen else TextSecondary)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedTab == 1) Color.White else Color.Transparent)
                    .clickable { selectedTab = 1 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Resueltas  $resolvedCount", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (selectedTab == 1) PrimaryGreen else TextSecondary)
            }
        }

        Text(
            if (selectedTab == 0) "Requieren atención" else "Historial de resueltas",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Text("Ordenadas por prioridad", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(horizontal = 16.dp))

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (selectedTab == 0) "No tienes alertas activas" else "No hay alertas resueltas", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList, key = { it.id }) { item ->
                    AlertItemCard(
                        data = item,
                        onClick = openDetail,
                        onMarkReviewed = {
                            val index = alertsList.indexOfFirst { it.id == item.id }
                            if (index != -1) {
                                alertsList[index] = alertsList[index].copy(isResolved = true)
                                Toast.makeText(context, "Alerta resuelta", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertItemCard(
    data: AlertItemData,
    onClick: () -> Unit,
    onMarkReviewed: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(data.levelBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(data.icon, contentDescription = null, tint = data.levelColor)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(data.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(data.levelBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("● ${data.level}", color = data.levelColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(data.value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(data.location, fontSize = 12.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(data.time, fontSize = 11.sp, color = TextSecondary)

                if (!data.isResolved) {
                    Button(
                        onClick = onMarkReviewed,
                        colors = ButtonDefaults.buttonColors(containerColor = LightGreenBg),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Marcar revisada", color = PrimaryGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Resuelta", color = PrimaryGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. PANTALLA CUARTELES
// ==========================================
@Composable
private fun QuartersScreen(
    isRiegoActive: Boolean,
    onSelectQuarter: () -> Unit
) {
    var viewMode by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBarHeader(title = "Cuarteles", subtitle = "PREDIO BUIN")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("4 sectores", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                Text("44,9 hectáreas", fontSize = 12.sp, color = TextSecondary)
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE2E8F0))
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (viewMode == 0) Color.White else Color.Transparent)
                        .clickable { viewMode = 0 }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Lista", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (viewMode == 0) PrimaryGreen else TextSecondary)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (viewMode == 1) Color.White else Color.Transparent)
                        .clickable { viewMode = 1 }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Mapa Satelital", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (viewMode == 1) PrimaryGreen else TextSecondary)
                }
            }
        }

        if (viewMode == 0) {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    QuarterCard(
                        "01", "Cuartel Norte", "Cerezos · 12,4 ha", "71%", "2,1°",
                        if (isRiegoActive) "Riego Activo 💧" else "Atención",
                        if (isRiegoActive) LightGreenBg else CriticalRedBg,
                        if (isRiegoActive) PrimaryGreen else CriticalRed,
                        onSelectQuarter
                    )
                }
                item { QuarterCard("02", "Cuartel Central", "Nogales · 9,8 ha", "68%", "5,8°", "Normal", LightGreenBg, PrimaryGreen, onSelectQuarter) }
                item { QuarterCard("03", "Cuartel Sur", "Cerezos · 14,1 ha", "64%", "6,2°", "Normal", LightGreenBg, PrimaryGreen, onSelectQuarter) }
                item { QuarterCard("04", "Cuartel Poniente", "Nogales · 8,6 ha", "52%", "5,4°", "Revisar", WarningAmberBg, WarningAmber, onSelectQuarter) }
            }
        } else {
            DetailedAgronomicMap(isRiegoActive = isRiegoActive, onSelectQuarter = onSelectQuarter)
        }
    }
}

@Composable
private fun DetailedAgronomicMap(
    isRiegoActive: Boolean,
    onSelectQuarter: () -> Unit
) {
    var selectedMapLayer by remember { mutableIntStateOf(0) }
    var activeMarkerInfo by remember { mutableStateOf("Toca un sector para ver telemetría") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Capa Suelo", "Líneas Riego", "Térmica").forEachIndexed { idx, label ->
                FilterChip(
                    selected = selectedMapLayer == idx,
                    onClick = { selectedMapLayer = idx },
                    label = { Text(label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryGreen, selectedLabelColor = Color.White)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF2D3748))
                .border(2.dp, PrimaryGreen, RoundedCornerShape(16.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                drawLine(
                    color = Color(0xFF3182CE),
                    start = Offset(x = canvasWidth * 0.5f, y = 0f),
                    end = Offset(x = canvasWidth * 0.5f, y = canvasHeight),
                    strokeWidth = 6f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                drawLine(
                    color = if (isRiegoActive) Color(0xFF00E676) else Color(0xFF63B3ED),
                    start = Offset(x = 0f, y = canvasHeight * 0.25f),
                    end = Offset(x = canvasWidth, y = canvasHeight * 0.25f),
                    strokeWidth = 4f
                )
            }

            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isRiegoActive) Color(0xFF1B4332).copy(alpha = 0.85f) else Color(0xFF7F1D1D).copy(alpha = 0.85f))
                            .clickable { activeMarkerInfo = "Cuartel Norte: 12,4 ha · Temp: 2,1°C · Cerezos" },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = if (isRiegoActive) Color(0xFF00E676) else CriticalRed)
                            Text("CUARTEL 01", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF064E3B).copy(alpha = 0.85f))
                            .clickable { activeMarkerInfo = "Cuartel Central: 9,8 ha · Temp: 5,8°C · Nogales" },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryGreen)
                            Text("CUARTEL 02", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                        }
                    }
                }

                Row(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF064E3B).copy(alpha = 0.85f))
                            .clickable { activeMarkerInfo = "Cuartel Sur: 14,1 ha · Temp: 6,2°C" },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryGreen)
                            Text("CUARTEL 03", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF78350F).copy(alpha = 0.85f))
                            .clickable { activeMarkerInfo = "Cuartel Poniente: 8,6 ha · Temp: 5,4°C" },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = WarningAmber)
                            Text("CUARTEL 04", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().clickable { onSelectQuarter() }
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("INFORMACIÓN DE TELEMETRÍA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                    Text(activeMarkerInfo, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = PrimaryGreen)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun QuarterCard(code: String, title: String, subtitle: String, hum: String, temp: String, status: String, statusBg: Color, statusColor: Color, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(LightGreenBg), contentAlignment = Alignment.Center) {
                Text(code, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimaryGreen)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(statusBg).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text("● $status", color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(subtitle, fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(14.dp))
                        Text(" $hum", fontSize = 11.sp, color = TextSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AcUnit, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(14.dp))
                        Text(" $temp", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
        }
    }
}

// ==========================================
// 4. PANTALLA DETALLE DE CUARTEL
// ==========================================
@Composable
private fun QuarterDetailScreen(
    isRiegoActive: Boolean,
    onToggleRiego: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(if (!isRiegoActive) "Confirmar Riego Anti-Helada" else "Detener Riego", fontWeight = FontWeight.Bold) },
            text = { Text(if (!isRiegoActive) "¿Deseas activar los aspersores anti-helada en Cuartel Norte?" else "¿Confirmas que deseas apagar los aspersores?") },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        onToggleRiego(!isRiegoActive)
                        Toast.makeText(context, if (!isRiegoActive) "Riego Activado" else "Riego Detenido", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (!isRiegoActive) PrimaryGreen else CriticalRed)
                ) {
                    Text(if (!isRiegoActive) "Activar Ahora" else "Detener Riego", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackground)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = PrimaryGreen)
            }
            Column {
                Text("DETALLE DE CUARTEL", fontSize = 10.sp, color = PrimaryGreen, fontWeight = FontWeight.Bold)
                Text("Cuartel Norte", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Información general", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(if (isRiegoActive) LightGreenBg else CriticalRedBg).padding(horizontal = 8.dp, vertical = 2.dp)) {
                            Text(if (isRiegoActive) "💧 RIEGO ACTIVO" else "⚠️ RIESGO CRÍTICO", color = if (isRiegoActive) PrimaryGreen else CriticalRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("• Variedad: Cerezos Lapins", fontSize = 13.sp, color = TextSecondary)
                    Text("• Superficie: 12,4 hectáreas", fontSize = 13.sp, color = TextSecondary)
                    Text("• Sensores activos: 4/4", fontSize = 13.sp, color = TextSecondary)
                    Text("• Temperatura actual: 2,1 °C", fontSize = 13.sp, color = CriticalRed, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { showConfirmDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = if (!isRiegoActive) PrimaryGreen else CriticalRed),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(if (!isRiegoActive) Icons.Default.WaterDrop else Icons.Default.Stop, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (!isRiegoActive) "Activar Riego Anti-Helada" else "Detener Riego Anti-Helada", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// 5. PANTALLA HISTORIAL
// ==========================================
@Composable
private fun HistoryScreen(historyList: List<HistoryItemData>) {
    var selectedFilter by remember { mutableIntStateOf(0) }
    var isExpandedWithOlder by remember { mutableStateOf(false) }
    var selectedHistoryDetail by remember { mutableStateOf<HistoryItemData?>(null) }

    val filters = listOf("Todos", "Alertas", "Riego", "Sistema")
    val context = LocalContext.current

    val olderEvents = remember {
        listOf(
            HistoryItemData("7", "12 de junio · 22:15", "Calibración de sensor de radiación", "Estación R-01", PrimaryGreen, "Sistema", isOlder = true, details = "Ajuste de offset en piranómetro por técnico en terreno."),
            HistoryItemData("8", "12 de junio · 04:10", "Alerta preventiva por viento sur", "Predio Buin", WarningAmber, "Alertas", isOlder = true, details = "Ráfagas superaron los 28 km/h. Válvulas secundarias aseguradas."),
            HistoryItemData("9", "11 de junio · 14:00", "Riego de nutrición foliar", "Cuartel Central", PrimaryGreen, "Riego", isOlder = true, details = "Aplicación fertirriego enriquecido con potasio. 8.500 Litros.")
        )
    }

    val currentList = if (isExpandedWithOlder) historyList + olderEvents else historyList

    val filteredItems = currentList.filter { item ->
        when (selectedFilter) {
            1 -> item.category == "Alertas"
            2 -> item.category == "Riego"
            3 -> item.category == "Sistema"
            else -> true
        }
    }

    if (selectedHistoryDetail != null) {
        AlertDialog(
            onDismissRequest = { selectedHistoryDetail = null },
            title = { Text(selectedHistoryDetail!!.title, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column {
                    Text("📍 ${selectedHistoryDetail!!.location}", fontWeight = FontWeight.Bold, color = PrimaryGreen, fontSize = 12.sp)
                    Text("🕒 ${selectedHistoryDetail!!.time}", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(selectedHistoryDetail!!.details, fontSize = 13.sp, color = TextPrimary)
                }
            },
            confirmButton = {
                Button(onClick = { selectedHistoryDetail = null }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)) {
                    Text("Cerrar", color = Color.White)
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBarHeader(title = "Historial", subtitle = "REGISTRO DE ACTIVIDADES")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEachIndexed { index, name ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selectedFilter == index) PrimaryGreen else Color.White)
                        .clickable { selectedFilter = index }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(name, color = if (selectedFilter == index) Color.White else TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Línea de tiempo", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("${filteredItems.size} eventos", fontSize = 12.sp, color = TextSecondary)
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredItems, key = { it.id }) { item ->
                HistoryItem(item = item, onClick = { selectedHistoryDetail = item })
            }

            if (!isExpandedWithOlder) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            isExpandedWithOlder = true
                            Toast.makeText(context, "Eventos anteriores cargados", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cargar eventos anteriores (11 y 12 de junio)", color = PrimaryGreen)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(item: HistoryItemData, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(item.dotColor))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.time, fontSize = 10.sp, color = TextSecondary)
                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                Text("🗺️ ${item.location}", fontSize = 11.sp, color = TextSecondary)
            }
            Icon(Icons.Default.Info, contentDescription = "Detalles", tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
    }
}

// ==========================================
// 6. PANTALLA PERFIL
// ==========================================
@Composable
private fun ProfileScreen(
    userName: String,
    userEmail: String,
    isAdmin: Boolean,
    onLogout: () -> Unit
) {
    var pushEnabled by remember { mutableStateOf(true) }
    var offlineEnabled by remember { mutableStateOf(true) }
    val context = LocalContext.current

    val initials = if (userName.length >= 2) userName.substring(0, 2).uppercase() else "US"

    Column(modifier = Modifier.fillMaxSize()) {
        TopBarHeader(title = "Mi cuenta", subtitle = "CONFIGURACIÓN")

        Column(modifier = Modifier.padding(16.dp)) {
            Card(
                colors = CardDefaults.cardColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(userName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            if (isAdmin) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(WarningAmber)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("ADMIN", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Text(userEmail, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        Text("Predio Buin", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Preferencias", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Notifications, contentDescription = null, tint = PrimaryGreen)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Alertas push", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Eventos críticos y advertencias", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                        Switch(
                            checked = pushEnabled,
                            onCheckedChange = { pushEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = PrimaryGreen)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = PrimaryGreen)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Datos sin conexión", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Conservar últimas lecturas", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                        Switch(
                            checked = offlineEnabled,
                            onCheckedChange = { offlineEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = PrimaryGreen)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                        onLogout()
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = CriticalRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar sesión", color = CriticalRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("FRUTIS Monitor · v1.0 MVP", modifier = Modifier.fillMaxWidth(), fontSize = 10.sp, color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Text("Desarrollado por ASTI", modifier = Modifier.fillMaxWidth(), fontSize = 10.sp, color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

// ==========================================
// COMPONENTE HEADER REUTILIZABLE
// ==========================================
@Composable
private fun TopBarHeader(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(subtitle, fontSize = 10.sp, color = PrimaryGreen, fontWeight = FontWeight.Bold)
            Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF2ECC71)))
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.Wifi, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("En línea", fontSize = 11.sp, color = TextSecondary)
        }
    }
}
