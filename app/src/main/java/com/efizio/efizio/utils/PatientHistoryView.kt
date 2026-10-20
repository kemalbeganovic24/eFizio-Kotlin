package com.efizio.efizio

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.efizio.efizio.data.FizioRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientHistoryView(
    repository: FizioRepository,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var recordsList by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // Funkcija za ponovno pretraživanje/osvježavanje liste
    fun refreshData(query: String) {
        if (query.length >= 2) {
            isLoading = true
            repository.getHidzamaRecordsByPatient(query) { results ->
                recordsList = results.sortedByDescending { (it["timestampMillis"] as? Long) ?: 0L }
                isLoading = false
            }
        } else {
            recordsList = emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Karton Pacijenata") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Nazad")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { query ->
                    searchQuery = query
                    refreshData(query)
                },
                label = { Text("Ukucajte ime pacijenta...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (searchQuery.length >= 2 && recordsList.isEmpty()) {
                Text(
                    "Nema sačuvanih hidžama za: $searchQuery",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else if (recordsList.isNotEmpty()) {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = recordsList.firstOrNull()?.get("patientName") as? String ?: searchQuery,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text("Statistika tretmana", fontSize = 12.sp, color = Color.Gray)
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "Ukupno hidžama: ${recordsList.size}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    itemsIndexed(recordsList) { index, record ->
                        val treatmentNumber = recordsList.size - index
                        val recordId = record["id"] as? String ?: ""
                        val currentDateStr = record["dateStr"] as? String ?: ""
                        val currentTimestamp = record["timestampMillis"] as? Long ?: System.currentTimeMillis()

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Hidžama #$treatmentNumber",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    // Klikom na datum ili olovčicu otvara se native Android DatePicker
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        modifier = Modifier.clickable {
                                            val calendar = Calendar.getInstance()
                                            calendar.timeInMillis = currentTimestamp

                                            DatePickerDialog(
                                                context,
                                                { _, year, month, dayOfMonth ->
                                                    val selectedCal = Calendar.getInstance().apply {
                                                        set(Calendar.YEAR, year)
                                                        set(Calendar.MONTH, month)
                                                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                                    }

                                                    val formatter = SimpleDateFormat("dd.MM.yyyy.", Locale.getDefault())
                                                    val newDateStr = formatter.format(selectedCal.time)
                                                    val newTimestamp = selectedCal.timeInMillis

                                                    repository.updateHidzamaRecordDate(
                                                        recordId = recordId,
                                                        newDateStr = newDateStr,
                                                        newTimestampMillis = newTimestamp
                                                    ) {
                                                        refreshData(searchQuery)
                                                    }
                                                },
                                                calendar.get(Calendar.YEAR),
                                                calendar.get(Calendar.MONTH),
                                                calendar.get(Calendar.DAY_OF_MONTH)
                                            ).show()
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = currentDateStr,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Izmijeni datum",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }

                                Text("Usluga: ${record["service"] ?: "Hidžama"}", fontSize = 14.sp)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                Text("Detalji:", fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                                Text("• Broj čašica: ${record["cupsCount"] ?: "-"}")

                                val locs = (record["locations"] as? List<*>)?.joinToString(", ") ?: "-"
                                Text("• Lokacije: $locs")
                            }
                        }
                    }
                }
            }
        }
    }
}