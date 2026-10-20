package com.efizio.efizio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.efizio.efizio.data.Appointment
import com.efizio.efizio.data.FizioRepository
import com.efizio.efizio.ui.theme.ThemeToggleButton

val AccentGreen = Color(0xFF4CAF50)

@Composable
fun HomeView(
    repository: FizioRepository,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    var appointmentsList by remember { mutableStateOf<List<Appointment>>(emptyList()) }

    // Učitavanje termina u realnom vremenu iz Firestore-a
    LaunchedEffect(Unit) {
        repository.getAppointmentsSnapshot { updatedList ->
            appointmentsList = updatedList
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // Top Banner
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2937)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Fizioterapeut Ajdin Islamović",
                            color = AccentGreen,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    ThemeToggleButton(isDarkMode = isDarkMode, onToggle = onToggleTheme)
                }
            }

            // Search Bar


            Text("Termini", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            // Filtrirana lista iz učitane stanje promenljive
            val filteredList = appointmentsList.filter {
                searchText.isEmpty() || it.patientName.contains(searchText, ignoreCase = true)
            }

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nema zakazanih termina.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredList, key = { it.id }) { appt ->
                        AppointmentRow(
                            appointment = appt,
                            onDelete = {
                                repository.deleteAppointment(appt.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentRow(appointment: Appointment, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(appointment.patientName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(appointment.service, fontSize = 14.sp, color = Color.Gray)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${appointment.dateStr} - ${appointment.timeStr}", fontSize = 12.sp, color = Color.Gray)
                }

                Text("Ukupno: ${appointment.priceKM} KM", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            // CRVENO DUGME ZA BRISANJE
            IconButton(
                onClick = onDelete
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Izbriši termin",
                    tint = Color.Red
                )
            }
        }
    }
}