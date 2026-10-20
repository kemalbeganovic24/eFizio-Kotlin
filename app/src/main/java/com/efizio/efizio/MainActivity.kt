package com.efizio.efizio

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.efizio.efizio.data.FizioRepository
import com.efizio.efizio.ui.theme.EFizioTheme
import com.efizio.efizio.utils.LocalNotificationManager
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {

    private val repository by lazy { FizioRepository() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        LocalNotificationManager.createNotificationChannel(this)

        setContent {
            var isDarkMode by remember { mutableStateOf(false) }
            var selectedTab by remember { mutableIntStateOf(0) }

            // Traženje dozvole za notifikacije pri startu
            RequestNotificationPermission()

            EFizioTheme(isDarkMode = isDarkMode) {
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Početna") },
                                label = { Text("Početna") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Zakaži") },
                                label = { Text("Zakaži") }
                            )
                            // NOVO: Dodana 3. stavka za Karton Pacijenta
                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                icon = { Icon(Icons.Default.AssignmentInd, contentDescription = "Karton") },
                                label = { Text("Karton") }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (selectedTab) {
                            0 -> HomeView(
                                repository = repository,
                                isDarkMode = isDarkMode,
                                onToggleTheme = { isDarkMode = !isDarkMode }
                            )
                            1 -> BookingView(
                                repository = repository,
                                onBooked = { selectedTab = 0 },
                                onNavigateToPatientHistory = { selectedTab = 2 } // Otvara karton klikom na dugme iz BookingView-a
                            )
                            2 -> PatientHistoryView(
                                repository = repository,
                                onBackClick = { selectedTab = 0 } // Nazad na početnu
                            )
                        }
                    }
                }
            }
        }
    }
}

// Funkcija za traženje dozvole na Android 13+
@Composable
fun RequestNotificationPermission() {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}