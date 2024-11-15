package com.example.pikachu.view

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pikachu.ui.theme.PikachuTheme
import kotlin.math.PI
import kotlin.math.sin

class MagnetometerActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var sensor: Sensor? = null

    private var xValue by mutableStateOf(0f)
    private var yValue by mutableStateOf(0f)
    private var zValue by mutableStateOf(0f)

    private var isFetching = false
    private val handler = Handler(Looper.getMainLooper())

    // Logs to store the data from the sensors
    private var _logs = mutableStateListOf<LogEntry>()
    private val logs: List<LogEntry> get() = _logs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize sensor manager and sensor
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        setContent {
            PikachuTheme {
                MagnetometerUI(
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
        sensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun stopFetchingSensorData() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                xValue = it.values[0]
                yValue = it.values[1]
                zValue = it.values[2]

                _logs.add(LogEntry(xValue, yValue, zValue, time = System.currentTimeMillis()))
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    data class LogEntry(
        val x: Float,
        val y: Float,
        val z: Float,
        val time: Long
    )

    // Function to generate sine wave data based on sensor values
    fun generateSineWave(frequency: Double, sampleRate: Int, duration: Double, amplitude: Float): List<Float> {
        val samplesCount = (sampleRate * duration).toInt()
        return List(samplesCount) { i ->
            val time = i.toDouble() / sampleRate
            (amplitude * sin(2 * PI * frequency * time)).toFloat()
        }
    }

    @Composable
    fun MagnetometerUI(
        logs: List<LogEntry>,
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
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Start/Stop Button
                Button(
                    onClick = {
                        isOn = !isOn
                        onButtonClicked(isOn)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text(
                        text = if (isOn) "Stop Fetching Data" else "Start Fetching Data",
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Display Magnetometer values dynamically
                Text(
                    text = "Magnetometer Data",

                    textAlign = TextAlign.Center
                )

                Text(
                    text = "X: $xValue, Y: $yValue, Z: $zValue",

                )

                Spacer(modifier = Modifier.height(20.dp))

                // Display Clear Logs Button before the graph
                Button(
                    onClick = onClearLogs,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text(
                        text = "Clear Logs",
                    )
                }

                // Graph Section: Display real-time sine wave graph for the magnetometer values
                val sampleRate = 100
                val duration = 1.0 // 1 second duration for the sine wave
                val sineWaveX = generateSineWave(xValue.toDouble(), sampleRate, duration, xValue / 10)
                val sineWaveY = generateSineWave(yValue.toDouble(), sampleRate, duration, yValue / 10)
                val sineWaveZ = generateSineWave(zValue.toDouble(), sampleRate, duration, zValue / 10)

                MagnetometerGraph(
                    sineWaveX = sineWaveX,
                    sineWaveY = sineWaveY,
                    sineWaveZ = sineWaveZ,
                    modifier = Modifier
                        .weight(1f) // This ensures the graph takes up the remaining space
                        .fillMaxWidth() // Ensures the graph takes full width
                        .height(200.dp) // Set a fixed height for the graph
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Log List Section
                Text(
                    text = "Logs",

                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        logs.forEach { log ->
                            LogItem(log)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun MagnetometerGraph(
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
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                )
            }

            // Draw three sine waves for X, Y, and Z axes
            drawWave(sineWaveX, Color.Red)
            drawWave(sineWaveY, Color.Green)
            drawWave(sineWaveZ, Color.Blue)
        }
    }

    @Composable
    fun LogItem(log: LogEntry) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(1.dp, Color(0xFF455A64), RoundedCornerShape(8.dp)),
            color = Color(0xFF263238),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
            ) {
                Text(
                    text = "X: ${log.x} Y: ${log.y} Z: ${log.z}"
                )
                Text(
                    text = "Time: ${log.time}"
                )
            }
        }
    }
}
