package com.example.pikachu.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.pikachu.ui.theme.PikachuTheme
import com.google.android.gms.location.*

class GPSActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var isTracking = mutableStateOf(false)

    // Mutable list for logs
    private val _logs = mutableStateListOf<GPSLogEntry>()
    private val logs: List<GPSLogEntry> get() = _logs

    private var latitude by mutableStateOf(0.0)
    private var longitude by mutableStateOf(0.0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize fused location provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.forEach { location ->
                    updateLocation(location)
                }
            }
        }

        // Request location permissions
        requestLocationPermission()

        setContent {
            PikachuTheme {
                SimpleUIWithGPSLogs(
                    logs = logs,
                    latitude = latitude,
                    longitude = longitude,
                    onButtonClicked = { isOn ->
                        if (isOn) startLocationUpdates() else stopLocationUpdates()
                    },
                    onClearLogs = { _logs.clear() }
                )
            }
        }
    }

    private fun requestLocationPermission() {
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (!isGranted) {
                    // Handle permission denied case here
                }
            }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        isTracking.value = true
        val locationRequest = LocationRequest.create().apply {
            interval = 1000 // 1 second
            fastestInterval = 500
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
    }

    private fun stopLocationUpdates() {
        isTracking.value = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun updateLocation(location: Location) {
        latitude = location.latitude
        longitude = location.longitude
        val currentTime = System.currentTimeMillis().toString()

        _logs.add(GPSLogEntry(latitude, longitude, currentTime))
    }

    data class GPSLogEntry(
        val latitude: Double,
        val longitude: Double,
        val time: String
    )
}

@Composable
fun SimpleUIWithGPSLogs(
    logs: List<GPSActivity.GPSLogEntry>,
    latitude: Double,
    longitude: Double,
    onButtonClicked: (Boolean) -> Unit,
    onClearLogs: () -> Unit
) {
    var isOn by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B263B))
            .padding(top = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        isOn = !isOn
                        onButtonClicked(isOn)
                    },
                    modifier = Modifier.size(width = 150.dp, height = 50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOn) Color(0xFF6AB04A) else Color(0xFFE71C23)
                    )
                ) {
                    Text(text = if (isOn) "Stop Tracking" else "Start Tracking", fontSize = 16.sp, color = Color.White)
                }
                Button(
                    onClick = { onClearLogs() },
                    modifier = Modifier.size(width = 150.dp, height = 50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A79DF))
                ) {
                    Text(text = "Clear Logs", fontSize = 16.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Latitude: $latitude, Longitude: $longitude",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Display log entries
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxHeight()
                    .padding(8.dp)
                    .weight(1f)
            ) {
                logs.forEach { log ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .border(1.dp, Color(0xFF455A64), RoundedCornerShape(4.dp))
                            .background(Color(0xFF263238).copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Time: ${log.time}", color = Color.White, fontSize = 14.sp)
                            Text("Latitude: ${log.latitude}", color = Color.White, fontSize = 14.sp)
                            Text("Longitude: ${log.longitude}", color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}
