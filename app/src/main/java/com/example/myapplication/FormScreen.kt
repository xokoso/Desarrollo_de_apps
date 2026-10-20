package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.draw.clip

@Composable
fun FormScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = remember { DatabaseHelper(context) }

    // Estados para los campos del formulario
    var selectedQuarter by remember { mutableStateOf("") }
    var eventType by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var observations by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    val quarters = listOf("Cuartel Norte", "Cuartel Central", "Cuartel Sur", "Cuartel Poniente")
    val eventTypes = listOf("Riesgo de Helada", "Caudal Irregular", "Nivel de Estanque", "Fallo de Sensor", "Otro")

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Éxito") },
            text = { Text("Reporte creado correctamente") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    onBack()
                }) {
                    Text("Aceptar")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(ScreenBackground).verticalScroll(rememberScrollState())) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().background(CardBackground).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = PrimaryGreen)
            }
            Text("Nuevo Reporte", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
            Text("Registro de Alerta", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimaryGreen)
            Text("Complete la información del evento detectado", fontSize = 13.sp, color = TextSecondary)

            Spacer(modifier = Modifier.height(24.dp))

            // Campo: Selección de Cuartel
            Text("Cuartel", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                quarters.forEach { quarter ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedQuarter == quarter) PrimaryGreen else Color.White)
                            .clickable { selectedQuarter = quarter }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            quarter.split(" ").last(),
                            fontSize = 11.sp,
                            color = if (selectedQuarter == quarter) Color.White else TextPrimary,
                            fontWeight = if (selectedQuarter == quarter) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo: Tipo de Evento
            Text("Tipo de Evento", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                eventTypes.forEach { type ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { eventType = type }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = (eventType == type), onClick = { eventType = type }, colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen))
                        Text(type, fontSize = 14.sp, color = TextPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo: Valor de la lectura
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text("Valor medido (ej: 2.1°C, 60%)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(focusedIndicatorColor = PrimaryGreen, focusedLabelColor = PrimaryGreen)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo: Observaciones
            OutlinedTextField(
                value = observations,
                onValueChange = { observations = it },
                label = { Text("Observaciones adicionales") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                colors = TextFieldDefaults.colors(focusedIndicatorColor = PrimaryGreen, focusedLabelColor = PrimaryGreen)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Botón Guardar
            Button(
                onClick = {
                    if (selectedQuarter.isEmpty() || eventType.isEmpty() || value.isEmpty()) {
                        Toast.makeText(context, "Por favor complete los campos obligatorios", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Funcionalidad segura: esta acción no guarda datos", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar Reporte", fontWeight = FontWeight.Bold)
            }
        }
    }
}
