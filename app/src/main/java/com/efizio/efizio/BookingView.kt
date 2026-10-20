package com.efizio.efizio

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.efizio.efizio.data.Appointment
import com.efizio.efizio.data.FizioRepository
import com.efizio.efizio.utils.LocalNotificationManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookingView(
    repository: FizioRepository,
    onBooked: () -> Unit,
    onNavigateToPatientHistory: () -> Unit
) {
    val context = LocalContext.current

    var patientName by remember { mutableStateOf("") }
    val calendar = remember { Calendar.getInstance() }
    var selectedDateMillis by remember { mutableLongStateOf(calendar.timeInMillis) }

    var startTimeSlot by remember { mutableStateOf<String?>("10:00") }
    var endTimeSlot by remember { mutableStateOf<String?>("11:00") }
    val selectedServices = remember { mutableStateMapOf<String, Int>() }

    //Stanja za Hidžama Picker
    var cupsCount by remember { mutableIntStateOf(10) }
    val availableLocations = listOf("Leđa", "Glava", "Vrat", "Noge", "Ruke", "Lumbarni dio")
    val selectedLocations = remember { mutableStateListOf<String>("Leđa") }

    val servicePrices = mapOf(
        "Masaža leđa" to 45,
        "Relax masaža čitavog tijela" to 60,
        "Sportska masaža čitavog tijela" to 80,
        "Antistres masaža glave i leđa" to 50,
        "Masaza lica" to 30,
        "Masaza lica" to 40,
        "Masaza stopala" to 25,
        "Gold luxury masaža 24k (lice+cijelo tijelo)" to 90,
        "Compex Sp6" to 30,
        "Tens" to 20,
        "Kinesi tape 10" to 10,
        "Kinesi tape 20" to 20,
        "Kinesi tape 30" to 30,
        "Hidžama" to 50,
        "Elektroterapija TENS" to 30,
        "Kineziterapija kucne posjete" to 60,
        "REBOOT One terapija" to 40
    )

    val timeSlots = listOf(
        "09:00", "09:15", "09:30", "09:45", "10:00",
        "10:15", "10:30", "10:45", "11:00",
        "11:15", "11:30", "11:45", "12:00",
        "12:15", "12:30", "12:45", "13:00",
        "13:15", "13:30", "13:45", "14:00",
        "14:15", "14:30", "14:45", "15:00",
        "15:15", "15:30", "15:45", "16:00",
        "16:15", "16:30", "16:45", "17:00",
        "17:15", "17:30", "17:45", "18:00",
        "18:15", "18:30", "18:45", "19:00",
        "19:15", "19:30", "19:45", "20:00",
        "20:15", "20:30", "20:45", "21:00",
        "21:15", "21:30", "21:45", "22:00"
    )

    val totalCost = selectedServices.values.sum()

    val formattedDate = remember(selectedDateMillis) {
        SimpleDateFormat("dd.MM.yyyy.", Locale.getDefault()).format(Date(selectedDateMillis))
    }

    val timeRangeStr = if (startTimeSlot != null && endTimeSlot != null) "$startTimeSlot - $endTimeSlot" else startTimeSlot ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Zakaži Novi Termin", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = patientName,
            onValueChange = { patientName = it },
            label = { Text("Ime i prezime klijenta") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = {
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        calendar.set(year, month, dayOfMonth)
                        selectedDateMillis = calendar.timeInMillis
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Odabrani datum: $formattedDate")
        }

        Text("Vrijeme: $timeRangeStr", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.height(180.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(timeSlots) { slot ->
                val isSelected = slot == startTimeSlot || slot == endTimeSlot
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            if (startTimeSlot == null || endTimeSlot != null) {
                                startTimeSlot = slot
                                endTimeSlot = null
                            } else {
                                endTimeSlot = slot
                            }
                        }
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        slot,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Text("Odaberi usluge:", fontWeight = FontWeight.SemiBold)

        servicePrices.forEach { (service, price) ->
            val isChecked = selectedServices.containsKey(service)

            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isChecked) Color(0xFF1E3A8A).copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            if (isChecked) selectedServices.remove(service) else selectedServices[service] = price
                        }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(service)
                    Text("$price KM", fontWeight = FontWeight.Bold)
                }

                if (service == "Hidžama" && isChecked) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Postavke Hidžame:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Broj čašica:")
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { if (cupsCount > 1) cupsCount-- },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text("$cupsCount", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                                    IconButton(
                                        onClick = { cupsCount++ },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Text("Lokacije:")
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                availableLocations.forEach { loc ->
                                    val isLocSelected = selectedLocations.contains(loc)
                                    FilterChip(
                                        selected = isLocSelected,
                                        onClick = {
                                            if (isLocSelected) selectedLocations.remove(loc)
                                            else selectedLocations.add(loc)
                                        },
                                        label = { Text(loc, fontSize = 12.sp) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Ukupno za naplatiti:", fontWeight = FontWeight.Bold)
                Text("$totalCost KM", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
            }
        }

        val canSubmit = patientName.isNotEmpty() && selectedServices.isNotEmpty() && startTimeSlot != null

        Button(
            onClick = {
                val appointmentCalendar = Calendar.getInstance().apply {
                    timeInMillis = selectedDateMillis
                    startTimeSlot?.let { time ->
                        val parts = time.split(":")
                        if (parts.size == 2) {
                            set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                            set(Calendar.MINUTE, parts[1].toInt())
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                    }
                }

                val exactAppointmentTimeMillis = appointmentCalendar.timeInMillis

                val newAppt = Appointment(
                    patientName = patientName,
                    service = selectedServices.keys.joinToString(", "),
                    dateStr = formattedDate,
                    timeStr = timeRangeStr,
                    timestampMillis = exactAppointmentTimeMillis,
                    priceKM = totalCost
                )

                val isHidzamaSelected = selectedServices.containsKey("Hidžama")

                repository.addAppointmentWithHidzama(
                    appointment = newAppt,
                    isHidzama = isHidzamaSelected,
                    cupsCount = cupsCount,
                    locations = selectedLocations.toList()
                ) {
                    LocalNotificationManager.scheduleReminder(context, patientName, exactAppointmentTimeMillis)
                    patientName = ""
                    selectedServices.clear()
                    onBooked()
                }
            },
            enabled = canSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Spremi Termin", fontSize = 16.sp)
        }
    }
}