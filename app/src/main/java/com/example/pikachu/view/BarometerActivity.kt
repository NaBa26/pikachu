package com.example.pikachu.view

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pikachu.ui.theme.PikachuTheme
import kotlin.math.PI
import kotlin.math.sin

class BarometerActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var barometerSensor: Sensor? = null
    private var isFetching = false
    private var pressureValue by mutableStateOf(0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        barometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

        setContent {
            PikachuTheme {
                BarometerUI(
                    pressureValue = pressureValue,
                    onButtonClicked = { isOn ->
                        isFetching = isOn
                        if (isFetching) {
                            startFetchingSensorData()
                        } else {
                            stopFetchingSensorData()
                        }
                    }
                )
            }
        }
    }

    private fun startFetchingSensorData() {
        barometerSensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun stopFetchingSensorData() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (event.sensor.type == Sensor.TYPE_PRESSURE) {
                pressureValue = it.values[0]
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    @Composable
    fun BarometerUI(pressureValue: Float, onButtonClicked: (Boolean) -> Unit) {
        var isOn by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black) // Set background to black
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Barometer Data", fontSize = 24.sp, color = Color.White) // Set text color to white

                Spacer(modifier = Modifier.height(20.dp))

                // Start/Stop Button
                Button(onClick = {
                    isOn = !isOn
                    onButtonClicked(isOn)
                }) {
                    Text(text = if (isOn) "Stop" else "Start")
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Display the barometer value
                Text(text = "Pressure: ${pressureValue} hPa", fontSize = 18.sp, color = Color.White) // Set text color to white

                Spacer(modifier = Modifier.height(40.dp))

                // Generate and show sine wave based on pressure
                SineWaveGraph(pressureValue = pressureValue)
            }
        }
    }

    // Function to generate a sine wave based on pressure data
    fun generateSineWave(frequency: Double, amplitude: Float): List<Float> {
        val sampleRate = 100
        val duration = 1.0 // 1 second duration for the sine wave
        val samplesCount = (sampleRate * duration).toInt()
        return List(samplesCount) { i ->
            val time = i.toDouble() / sampleRate
            (amplitude * sin(2 * PI * frequency * time)).toFloat()
        }
    }

    @Composable
    fun SineWaveGraph(pressureValue: Float, modifier: Modifier = Modifier) {
        val sineWave = generateSineWave(pressureValue.toDouble(), pressureValue / 10)

        Canvas(modifier = modifier.height(150.dp)) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val maxAmplitude = sineWave.maxOrNull() ?: 1f
            val minAmplitude = sineWave.minOrNull() ?: -1f
            val normalizedData = sineWave.map { value ->
                ((value - minAmplitude) / (maxAmplitude - minAmplitude) * canvasHeight).toFloat()
            }

            drawPath(
                path = androidx.compose.ui.graphics.Path().apply {
                    sineWave.indices.forEach { index ->
                        val x = (index.toFloat() / sineWave.size) * canvasWidth
                        val y = canvasHeight - normalizedData[index]
                        if (index == 0) moveTo(x, y) else lineTo(x, y)
                    }
                },
                color = Color.Red,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}
