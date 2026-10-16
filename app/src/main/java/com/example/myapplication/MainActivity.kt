package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
data class AlertData(
    val id: Int,
    val title: String,
    val value: String,
    val level: String,
    val levelColor: Color,
    val levelBg: Color,
    val location: String,
    val time: String,
    val icon: ImageVector,
    var isReviewed: Boolean = false
)

data class HistoryEvent(
    val time: String,
    val title: String,
    val location: String,
    val dotColor: Color,
    val category: String,
    val detail: String
)

// --- NAVEGACIÓN ---
enum class Screen(val label: String, val icon: ImageVector, val badgeCount: Int = 0) {
    Login("Login", Icons.Default.Lock),
    Register("Registro", Icons.Default.PersonAdd),
    Home("Inicio", Icons.Default.Home),
    Alerts("Alertas", Icons.Default.Notifications, badgeCount = 0),
    Quarters("Cuarteles", Icons.AutoMirrored.Filled.List),
    History("Historial", Icons.Default.History),
    Profile("Perfil", Icons.Default.Person),
    Detail("Detalle", Icons.Default.Info),
    SystemStatus("Sistema", Icons.Default.Settings),
    Form("Formulario", Icons.Default.Add)
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
    val context = LocalContext.current
    val userName = remember { UserSession.getFullName(context) ?: "Usuario" }
    val userRole = remember { UserSession.getUserRole(context) }
    var screen by remember {
        mutableStateOf(
            if (UserSession.isLoggedIn(context)) Screen.Home else Screen.Login
        )
    }
    val selectedNavigation = if (screen == Screen.Detail || screen == Screen.SystemStatus || screen == Screen.Form) Screen.Home else screen


    // Estado para las alertas activas (Mock para el badge, se puede ampliar a BD después)
    var alerts by remember {
        mutableStateOf(
            listOf(
                AlertData(1, "Riesgo de helada", "2,1 °C", "Crítica", CriticalRed, CriticalRedBg, "Cuartel Norte · Estación T-01", "Hoy, 06:42", Icons.Default.AcUnit),
                AlertData(2, "Caudal irregular", "-32%", "Media", WarningAmber, WarningAmberBg, "Cuartel 04 · Línea secundaria", "Hoy, 06:26", Icons.Default.WaterDrop),
                AlertData(3, "Nivel estanque bajo", "19%", "Media", WarningAmber, WarningAmberBg, "Estanque principal", "Ayer, 18:14", Icons.Default.FormatColorFill)
            )
        )
    }

    Scaffold(
        containerColor = ScreenBackground,
        bottomBar = {
            if (screen != Screen.Login && screen != Screen.Register && screen != Screen.Form) {
                NavigationBar(containerColor = Color.White) {
                    Screen.entries
                        .filter { it != Screen.Login && it != Screen.Register && it != Screen.Detail && it != Screen.SystemStatus && it != Screen.Form }
                        .forEach { destination ->
                            NavigationBarItem(
                                selected = selectedNavigation == destination,
                                onClick = { screen = destination },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            val activeCount = alerts.count { !it.isReviewed }
                                            if (destination == Screen.Alerts && activeCount > 0) {
                                                Badge(containerColor = CriticalRed, contentColor = Color.White) {
                                                    Text("$activeCount")
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
                                        fontSize = 11.sp
                                    )
                                }
                            )
                        }
                }
            }
        },
        floatingActionButton = {
            if (screen == Screen.Home) {
                FloatingActionButton(
                    onClick = { screen = Screen.Form },
                    containerColor = PrimaryGreen,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo Reporte")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (screen) {
                Screen.Login -> LoginScreen(
                    onLoginSuccess = { screen = Screen.Home },
                    onNavigateToRegister = { screen = Screen.Register }
                )
                Screen.Register -> RegisterScreen(
                    onRegisterSuccess = { screen = Screen.Login },
                    onBackToLogin = { screen = Screen.Login }
                )
                Screen.Home -> HomeScreen(
                    openAlerts = { screen = Screen.Alerts },
                    openQuarters = { screen = Screen.Quarters },
                    openDetail = { screen = Screen.Detail },
                    openSystem = { screen = Screen.SystemStatus }
                )
                Screen.Alerts -> AlertsScreen(
                    alerts = alerts,
                    onMarkReviewed = { id ->
                        alerts = alerts.map { if (it.id == id) it.copy(isReviewed = true) else it }
                    },
                    openDetail = { screen = Screen.Detail }
                )
                Screen.Quarters -> QuartersScreen(onSelectQuarter = { screen = Screen.Detail })
                Screen.Detail -> QuarterDetailScreen(onBack = { screen = Screen.Quarters })
                Screen.History -> HistoryScreen()
                Screen.Profile -> ProfileScreen(onLogout = {
                    UserSession.logout(context)
                    screen = Screen.Login
                })
                Screen.SystemStatus -> SystemStatusScreen(onBack = { screen = Screen.Home })
                Screen.Form -> FormScreen(onBack = { screen = Screen.Home })
            }
        }
    }
}

// ==========================================
// PANTALLAS
// ==========================================

@Composable
private fun HomeScreen(openAlerts: () -> Unit, openQuarters: () -> Unit, openDetail: () -> Unit, openSystem: () -> Unit) {
    val context = LocalContext.current
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Box(modifier = Modifier.fillMaxWidth().background(PrimaryGreen).padding(20.dp)) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BarChart, contentDescription = null, tint = Color(0xFF6EE7B7), modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("FRUTIS MONITOR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        IconButton(onClick = openAlerts, modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.15f))) {
                            Icon(Icons.Outlined.Notifications, contentDescription = "Alertas", tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Buenos días, $userName", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Predio Buin · Última lectura hace 2 min", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
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
                    modifier = Modifier.fillMaxWidth().offset(y = (-20).dp).clickable { openSystem() }
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(0xFF2ECC71)))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Sistema operativo", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("12 de 13 dispositivos conectados", fontSize = 12.sp, color = TextSecondary)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
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
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Atención requerida", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    Text("Ver todas", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.clickable { openAlerts() })
                }
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth().clickable { openDetail() }
                ) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(CriticalRedBg), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AcUnit, contentDescription = null, tint = CriticalRed)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Riesgo de helada", fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(CriticalRedBg).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                    Text("CRÍTICA", color = CriticalRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Cuartel Norte · 2,1 °C", fontSize = 12.sp, color = TextSecondary)
                            Text("Hace 8 minutos", fontSize = 11.sp, color = TextSecondary)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
                    }
                }
            }
        }
    }
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

@Composable
private fun AlertsScreen(alerts: List<AlertData>, onMarkReviewed: (Int) -> Unit, openDetail: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    val activeAlerts = alerts.filter { !it.isReviewed }
    val resolvedAlerts = alerts.filter { it.isReviewed }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBarHeader(title = "Alertas", subtitle = "CENTRO DE EVENTOS")
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFE2E8F0)).padding(4.dp)) {
            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (selectedTab == 0) Color.White else Color.Transparent).clickable { selectedTab = 0 }.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                Text("Activas ${activeAlerts.size}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryGreen)
            }
            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (selectedTab == 1) Color.White else Color.Transparent).clickable { selectedTab = 1 }.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                Text("Resueltas ${resolvedAlerts.size}", fontWeight = FontWeight.Medium, fontSize = 13.sp, color = TextSecondary)
            }
        }

        if (selectedTab == 0) {
            Text("Requieren atención", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 16.dp))
            Text("Ordenadas por prioridad", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(activeAlerts) { alert ->
                    AlertItemCard(alert, onClick = openDetail) {
                        onMarkReviewed(alert.id)
                        Toast.makeText(context, "Alerta revisada", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Text("Historial de alertas", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(resolvedAlerts) { alert ->
                    AlertItemCard(alert, onClick = openDetail, onMarkReviewed = {})
                }
            }
        }
    }
}

@Composable
private fun AlertItemCard(alert: AlertData, onClick: () -> Unit, onMarkReviewed: () -> Unit = {}) {
    Card(colors = CardDefaults.cardColors(containerColor = CardBackground), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(1.dp), modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(alert.levelBg), contentAlignment = Alignment.Center) {
                    Icon(alert.icon, contentDescription = null, tint = alert.levelColor)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(alert.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                    Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(alert.levelBg).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text("● ${alert.level}", color = alert.levelColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(alert.value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(alert.location, fontSize = 12.sp, color = TextSecondary)
            if (!alert.isReviewed) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(alert.time, fontSize = 11.sp, color = TextSecondary)
                    Button(onClick = onMarkReviewed, colors = ButtonDefaults.buttonColors(containerColor = LightGreenBg), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Marcar revisada", color = PrimaryGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuartersScreen(onSelectQuarter: () -> Unit) {
    var viewMode by remember { mutableIntStateOf(0) }
    Column(modifier = Modifier.fillMaxSize()) {
        TopBarHeader(title = "Cuarteles", subtitle = "PREDIO BUIN")
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("4 sectores", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                Text("44,9 hectáreas", fontSize = 12.sp, color = TextSecondary)
            }
            Row(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Color(0xFFE2E8F0)).padding(2.dp)) {
                Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(if (viewMode == 0) Color.White else Color.Transparent).clickable { viewMode = 0 }.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text("Lista", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                }
                Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(if (viewMode == 1) Color.White else Color.Transparent).clickable { viewMode = 1 }.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text("Mapa", fontSize = 12.sp, color = TextSecondary)
                }
            }
        }
        if (viewMode == 0) {
            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { QuarterCard("01", "Cuartel Norte", "Cerezos · 12,4 ha", "71%", "2,1°", "Atención", CriticalRedBg, CriticalRed, onSelectQuarter) }
                item { QuarterCard("02", "Cuartel Central", "Nogales · 9,8 ha", "68%", "5,8°", "Normal", LightGreenBg, PrimaryGreen, onSelectQuarter) }
                item { QuarterCard("03", "Cuartel Sur", "Cerezos · 14,1 ha", "64%", "6,2°", "Normal", LightGreenBg, PrimaryGreen, onSelectQuarter) }
                item { QuarterCard("04", "Cuartel Poniente", "Nogales · 8,6 ha", "52%", "5,4°", "Revisar", WarningAmberBg, WarningAmber, onSelectQuarter) }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Mapa Esquemático del Predio", fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(20.dp))
                    Row {
                        QuarterMapBox("Norte", CriticalRed, onSelectQuarter)
                        Spacer(modifier = Modifier.width(8.dp))
                        QuarterMapBox("Este", PrimaryGreen, onSelectQuarter)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        QuarterMapBox("Oeste", WarningAmber, onSelectQuarter)
                        Spacer(modifier = Modifier.width(8.dp))
                        QuarterMapBox("Sur", PrimaryGreen, onSelectQuarter)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuarterMapBox(name: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.3f))
            .clickable { onClick() }
            .border(2.dp, color, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(name, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun QuarterCard(code: String, title: String, subtitle: String, hum: String, temp: String, status: String, statusBg: Color, statusColor: Color, onClick: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = CardBackground), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(1.dp), modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
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

@Composable
private fun QuarterDetailScreen(onBack: () -> Unit) {
    var irrigationActive by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().background(CardBackground).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = PrimaryGreen)
            }
            Column {
                Text("DETALLE DE CUARTEL", fontSize = 10.sp, color = PrimaryGreen, fontWeight = FontWeight.Bold)
                Text("Cuartel Norte", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        }
        Column(modifier = Modifier.padding(16.dp)) {
            Card(colors = CardDefaults.cardColors(containerColor = CardBackground), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Información general", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Variedad: Cerezos Lapins", fontSize = 13.sp, color = TextSecondary)
                    Text("Superficie: 12,4 hectáreas", fontSize = 13.sp, color = TextSecondary)
                    Text("Sensores activos: 4/4", fontSize = 13.sp, color = TextSecondary)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    irrigationActive = !irrigationActive
                    Toast.makeText(context, if (irrigationActive) "Riego Anti-Helada ACTIVADO" else "Riego Anti-Helada DESACTIVADO", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (irrigationActive) Color.Gray else PrimaryGreen),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (irrigationActive) "Desactivar Riego Anti-Helada" else "Activar Riego Anti-Helada", color = Color.White)
            }
        }
    }
}

@Composable
private fun HistoryScreen() {
    var selectedFilter by remember { mutableIntStateOf(0) }
    val filters = listOf("Todos", "Alertas", "Riego", "Sistema")
    var selectedEvent by remember { mutableStateOf<HistoryEvent?>(null) }
    val context = LocalContext.current
    val db = remember { DatabaseHelper(context) }
    val userRole = remember { UserSession.getUserRole(context) }

    val dbEvents = remember(selectedFilter) {
        db.getAllAlerts().map {
            HistoryEvent(
                time = it["fecha"] ?: "",
                title = it["tipo_evento"] ?: "Evento",
                location = it["cuartel"] ?: "Desconocido",
                dotColor = PrimaryGreen,
                category = it["tipo_evento"] ?: "Alertas",
                detail = it["observaciones"] ?: "Sin detalles"
            )
        }
    }

    val mockEvents = listOf(
        HistoryEvent("Hoy · 08:00", "Riesgo de helada", "Cuartel Norte", CriticalRed, "Alertas", "Caída brusca de temperatura a 1.5°C. Se activó riego anti-helada."),
        HistoryEvent("Hoy · 09:30", "Caudal Irregular", "Cuartel Sur", WarningAmber, "Alertas", "Baja presión detectada en válvula V-02. Se requiere revisión técnica."),
        HistoryEvent("Hoy · 10:15", "Riego Programado", "Cuartel Central", PrimaryGreen, "Riego", "Ciclo de riego matutino completado exitosamente en sector B."),
        HistoryEvent("Ayer · 14:00", "Fallo de Sensor", "Cuartel Poniente", CriticalRed, "Sistema", "Sensor de humedad S-12 fuera de línea. Reinicio remoto fallido."),
        HistoryEvent("Ayer · 16:45", "Nivel Estanque", "Estanque Principal", WarningAmber, "Alertas", "Nivel crítico al 15%. Se inició bombeo de emergencia."),
        HistoryEvent("Ayer · 18:00", "Riego Nocturno", "Cuartel Norte", PrimaryGreen, "Riego", "Suministro de agua activado para prevención de heladas nocturnas."),
        HistoryEvent("Anteayer · 07:00", "Lectura Estable", "Cuartel Central", PrimaryGreen, "Sistema", "Sincronización de datos completada. Todos los sensores operativos."),
        HistoryEvent("Anteayer · 11:20", "Caudal Alto", "Cuartel Sur", WarningAmber, "Alertas", "Sobrepresión detectada en línea secundaria. Válvula de escape activada."),
        HistoryEvent("Anteayer · 15:00", "Mantenimiento", "Cuartel Poniente", PrimaryGreen, "Sistema", "Sustitución de filtro de agua en estación T-04 finalizada."),
        HistoryEvent("Anteayer · 20:00", "Cierre Riego", "Cuartel Norte", PrimaryGreen, "Riego", "Ciclo de riego finalizado según programación horaria.")
    )

    val allEvents = dbEvents + mockEvents
    val filteredEvents = if (selectedFilter == 0) allEvents else allEvents.filter { it.category == filters[selectedFilter] }

    if (selectedEvent != null) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            IconButton(onClick = { selectedEvent = null }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = PrimaryGreen)
            }
            Text(selectedEvent!!.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(selectedEvent!!.time, color = TextSecondary)
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBackground)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Detalle del Evento", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(selectedEvent!!.detail, color = TextSecondary)
                }
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBarHeader(title = "Historial", subtitle = "REGISTRO de ACTIVIDADES")
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                filters.forEachIndexed { index, name ->
                    Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(if (selectedFilter == index) PrimaryGreen else Color.White).clickable { selectedFilter = index }.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(name, color = if (selectedFilter == index) Color.White else TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredEvents) { event ->
                    // Para permitir borrar, necesitamos el ID.
                    // Nota: HistoryEvent es un mock simple, buscaremos el ID en la BD basándonos en el tiempo/título si es necesario,
                    // pero lo ideal es que HistoryEvent tuviera el ID.
                    // Como estamos refactorizando, vamos a pasar la acción de borrar.
                    HistoryItem(
                        event = event,
                        onClick = { selectedEvent = event },
                        onDelete = {
                            // Buscamos el ID del reporte en la BD para borrarlo
                            // Esto es una simplificación ya que allEvents ahora viene de la BD
                            val all = db.getAllAlerts()
                            val idToDelete = all.find { it["fecha"] == event.time && it["tipo_evento"] == event.title }?.get("id")?.toIntOrNull()
                            if (idToDelete != null) {
                                db.deleteAlert(idToDelete)
                                Toast.makeText(context, "Reporte eliminado", Toast.LENGTH_SHORT).show()
                                // Forzamos refresco (en una app real usaríamos StateFlow o ViewModel)
                            }
                        },
                        isAdmin = (userRole == "Administrador")
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(event: HistoryEvent, onClick: () -> Unit, onDelete: (() -> Unit)? = null, isAdmin: Boolean = false) {
    Card(colors = CardDefaults.cardColors(containerColor = CardBackground), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(event.dotColor))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(event.time, fontSize = 10.sp, color = TextSecondary)
                Text(event.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                Text("🗺️ ${event.location}", fontSize = 11.sp, color = TextSecondary)
            }
            if (isAdmin && onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = CriticalRed, modifier = Modifier.size(18.dp))
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
        }
    }
}

@Composable
private fun ProfileScreen(onLogout: () -> Unit) {
    var pushEnabled by remember { mutableStateOf(true) }
    var offlineEnabled by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val userName = remember { UserSession.getFullName(context) ?: "Usuario" }
    val userRole = remember { UserSession.getUserRole(context) ?: "Usuario" }
    val initial = userName.firstOrNull()?.toString()?.uppercase() ?: "?"

    Column(modifier = Modifier.fillMaxSize()) {
        TopBarHeader(title = "Mi cuenta", subtitle = "CONFIGURACIÓN")
        Column(modifier = Modifier.padding(16.dp)) {
            Card(colors = CardDefaults.cardColors(containerColor = PrimaryGreen), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                        Text(initial, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(userName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(userRole, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        Text("Predio Buin", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text("Preferencias", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(12.dp))
            Card(colors = CardDefaults.cardColors(containerColor = CardBackground), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Notifications, contentDescription = null, tint = PrimaryGreen)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Alertas push", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Eventos críticos y advertencias", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                        Switch(checked = pushEnabled, onCheckedChange = { pushEnabled = it }, colors = SwitchDefaults.colors(checkedThumbColor = PrimaryGreen))
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = PrimaryGreen)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Datos sin conexión", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Conservar últimas lecturas", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                        Switch(checked = offlineEnabled, onCheckedChange = { offlineEnabled = it }, colors = SwitchDefaults.colors(checkedThumbColor = PrimaryGreen))
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth().clickable { onLogout() }, horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = CriticalRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar sesión", color = CriticalRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun SystemStatusScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = PrimaryGreen) }
            Text("Estado del Sistema", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items((1..13).toList()) { i ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).background(CardBackground).padding(12.dp).clip(RoundedCornerShape(8.dp)), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(if (i < 13) Color(0xFF2ECC71) else Color.Gray))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Dispositivo Sensor #$i", modifier = Modifier.weight(1f))
                    Text(if (i < 13) "Conectado" else "Desconectado", fontSize = 12.sp, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun TopBarHeader(title: String, subtitle: String) {
    Row(modifier = Modifier.fillMaxWidth().background(CardBackground).padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
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