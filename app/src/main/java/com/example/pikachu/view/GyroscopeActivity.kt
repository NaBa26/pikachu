package com.example.pikachu.view

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pikachu.ui.theme.PikachuTheme
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin
class GyroscopeActivity : ComponentActivity(), SensorEventListener {
    private val handler = Handler(Looper.getMainLooper())
    private var isFetching = false
    private lateinit var sensorManager: SensorManager
    private var sensor: Sensor? = null
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    // Mutable list of log entries that updates the UI whenever modified
    private var _logs = mutableStateListOf<LogEntry>()
    private val logs: List<LogEntry> get() = _logs

    private var xValue by mutableStateOf(0f)
    private var yValue by mutableStateOf(0f)
    private var zValue by mutableStateOf(0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize sensor manager and gyroscope sensor
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        setContent {
            PikachuTheme {
                SimpleUIWithLogs(
                    logs = logs,
                    xValue = xValue,
                    yValue = yValue,
                    zValue = zValue,
                    onButtonClicked = { isOn ->
                        isFetching = isOn
                        if (isFetching) {
                            startFetchingSensorData()
                        } else {
                            stopFetchingSensorData()
                        }
                    },
                    onClearLogs = { _logs.clear() }
                )
            }
        }
    }

    private fun startFetchingSensorData() {
        sensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun stopFetchingSensorData() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            xValue = it.values[0]
            yValue = it.values[1]
            zValue = it.values[2]
            val currentTime = dateFormat.format(Date())

            _logs.add(LogEntry(xValue, yValue, zValue, latitude = 0.0, longitude = 0.0, time = currentTime))
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    data class LogEntry(
        val x: Float,
        val y: Float,
        val z: Float,
        val latitude: Double,
        val longitude: Double,
        val time: String
    )
}

// Function to generate a sine wave based on sensor data
fun generateSineWaveGyro(frequency: Double, sampleRate: Int, duration: Double, amplitude: Float): List<Float> {
    val samplesCount = (sampleRate * duration).toInt()
    return List(samplesCount) { i ->
        val time = i.toDouble() / sampleRate
        (amplitude * sin(2 * PI * frequency * time)).toFloat()
    }
}

@Composable
fun SineWaveGraphGyro(
    sineWaveX: List<Float>,
    sineWaveY: List<Float>,
    sineWaveZ: List<Float>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        fun drawWave(wave: List<Float>, color: Color) {
            val maxAmplitude = wave.maxOrNull() ?: 1f
            val minAmplitude = wave.minOrNull() ?: -1f

            val normalizedData = wave.map { value ->
                ((value - minAmplitude) / (maxAmplitude - minAmplitude) * canvasHeight).toFloat()
            }

            drawPath(
                path = androidx.compose.ui.graphics.Path().apply {
                    wave.indices.forEach { index ->
                        val x = (index.toFloat() / wave.size) * canvasWidth
                        val y = canvasHeight - normalizedData[index]
                        if (index == 0) moveTo(x, y) else lineTo(x, y)
                    }
                },
                color = color,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Draw three sine waves for X, Y, Z axes
        drawWave(sineWaveX, Color.Red)
        drawWave(sineWaveY, Color.Yellow)
        drawWave(sineWaveZ, Color.Blue)
    }
}

@Composable
fun AnimatedSineWaveGraphGyro(
    xValue: Float,
    yValue: Float,
    zValue: Float,
    modifier: Modifier = Modifier
) {
    val sampleRate = 100
    val duration = 1.0 // 1 second duration for the sine wave

    // Generate sine waves dynamically based on gyroscope values
    val sineWaveX = generateSineWaveGyro(xValue.toDouble(), sampleRate, duration, xValue / 10)
    val sineWaveY = generateSineWaveGyro(yValue.toDouble(), sampleRate, duration, yValue / 10)
    val sineWaveZ = generateSineWaveGyro(zValue.toDouble(), sampleRate, duration, zValue / 10)

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        fun drawWave(wave: List<Float>, color: Color) {
            val maxAmplitude = wave.maxOrNull() ?: 1f
            val minAmplitude = wave.minOrNull() ?: -1f

            val normalizedData = wave.map { value ->
                ((value - minAmplitude) / (maxAmplitude - minAmplitude) * canvasHeight).toFloat()
            }

            drawPath(
                path = androidx.compose.ui.graphics.Path().apply {
                    wave.indices.forEach { index ->
                        val x = (index.toFloat() / wave.size) * canvasWidth
                        val y = canvasHeight - normalizedData[index]
                        if (index == 0) moveTo(x, y) else lineTo(x, y)
                    }
                },
                color = color,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Draw three sine waves for X, Y, Z axes
        drawWave(sineWaveX, Color.Red)
        drawWave(sineWaveY, Color.Yellow)
        drawWave(sineWaveZ, Color.Blue)
    }
}

@Composable
fun SimpleUIWithLogs(
    logs: List<GyroscopeActivity.LogEntry>,
    xValue: Float,
    yValue: Float,
    zValue: Float,
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
                    Text(text = if (isOn) "Turn Off" else "Turn On", fontSize = 16.sp, color = Color.White)
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
                text = "Device: ${Build.MODEL} (${Build.MANUFACTURER})",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
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
                            Text(log.time, color = Color.White, fontSize = 14.sp)
                            Text("Gyroscope Data:", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("X: ${log.x}", color = Color.White, fontSize = 14.sp)
                            Text("Y: ${log.y}", color = Color.White, fontSize = 14.sp)
                            Text("Z: ${log.z}", color = Color.White, fontSize = 14.sp)
                            Text("Location:", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Latitude: ${log.latitude}, Longitude: ${log.longitude}", color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Real-time Animated Sine Wave Graph
            AnimatedSineWaveGraphGyro(
                xValue = xValue,
                yValue = yValue,
                zValue = zValue,
                modifier = Modifier.fillMaxWidth().height(150.dp)
            )
        }
    }
}
