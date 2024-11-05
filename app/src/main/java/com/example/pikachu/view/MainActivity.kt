package com.example.pikachu.view

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.icu.text.SimpleDateFormat
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.pikachu.ui.theme.PikachuTheme
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity(), SensorEventListener, LocationListener {
    private val handler = Handler(Looper.getMainLooper())
    private var isFetching = false
    private lateinit var sensorManager: SensorManager
    private var sensor: Sensor? = null
    private lateinit var locationManager: LocationManager
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    // Mutable list of log entries that updates the UI whenever modified
    private var _logs = mutableStateListOf<LogEntry>()
    private val logs: List<LogEntry> get() = _logs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize sensor and location managers
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        setContent {
            PikachuTheme {
                SimpleUIWithLogs(
                    logs = logs,
                    onButtonClicked = { isOn ->
                        isFetching = isOn
                        if (isFetching) {
                            startFetchingSensorData()
                        } else {
                            stopFetchingSensorData()
                        }
                    },
                    onClearLogs = {
                        _logs.clear()
                    }
                )
            }
        }
    }

    private fun startFetchingSensorData() {
        sensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

        // Check and request location permissions if not granted
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000, // Minimum update interval in milliseconds
                0f, this
            )
        }

        // Start location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)

        // Schedule continuous sensor data fetching every second
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (isFetching) {
                    handler.postDelayed(this, 5000)
                }
            }
        }, 5000)
    }


    private fun stopFetchingSensorData() {
        handler.removeCallbacksAndMessages(null)
        sensorManager.unregisterListener(this)
        locationManager.removeUpdates(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val x = it.values[0]
            val y = it.values[1]
            val z = it.values[2]
            val currentTime = dateFormat.format(Date())

            // Add log entry with accelerometer data, placeholder values for location, and timestamp
            _logs.add(LogEntry(x, y, z, latitude = 0.0, longitude = 0.0, time = currentTime))
        }
    }

    override fun onLocationChanged(location: Location) {
        val currentTime = dateFormat.format(Date())

        // Update the last log entry with new location data and timestamp
        val lastEntry = _logs.lastOrNull()
        if (lastEntry != null) {
            _logs[_logs.lastIndex] = lastEntry.copy(
                latitude = location.latitude,
                longitude = location.longitude,
                time = currentTime
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // Data class to represent each log entry
    data class LogEntry(val x: Float, val y: Float, val z: Float, val latitude: Double, val longitude: Double, val time: String)
}

@Composable
fun SimpleUIWithLogs(logs: List<MainActivity.LogEntry>, onButtonClicked: (Boolean) -> Unit, onClearLogs: () -> Unit) {
    var isOn by remember { mutableStateOf(false) }

    val deviceInfo = "Device: ${Build.MODEL} (${Build.MANUFACTURER})"
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        isOn = !isOn
                        onButtonClicked(isOn)
                    },
                    modifier = Modifier
                        .size(width = 150.dp, height = 50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOn) Color(0xFF6AB04A) else Color(0xFFE71C23)
                    )
                ) {
                    Text(
                        text = if (isOn) "Turn Off" else "Turn On",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
                Button(
                    onClick = { onClearLogs() },
                    modifier = Modifier
                        .size(width = 150.dp, height = 50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0A79DF)
                    )
                ) {
                    Text(
                        text = "Clear Logs",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = deviceInfo,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))
            // Display log entries in formatted text
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxHeight()
                    .padding(8.dp)
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
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(log.time,color = Color.White, fontSize = 14.sp)
                            Text("Accelerometer Data:", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("X: ${log.x}", color = Color.White, fontSize = 14.sp)
                            Text("Y: ${log.y}", color = Color.White, fontSize = 14.sp)
                            Text("Z: ${log.z}", color = Color.White, fontSize = 14.sp)
                            Text("Location:", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Latitude: ${log.latitude}, Longitude: ${log.longitude}", color = Color.White, fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}
