package com.example.pikachu.view

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
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

class LightSensorActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null

    private var lightLevel by mutableStateOf(0f)

    // Frequency will depend on the light level
    private var sineWaveFrequency by mutableStateOf(1.0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the sensor manager and light sensor
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        setContent {
            PikachuTheme {
                LightSensorUI(lightLevel = lightLevel, sineWaveFrequency = sineWaveFrequency)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Register the light sensor listener
        lightSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        // Unregister the light sensor listener to save battery
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && event.sensor.type == Sensor.TYPE_LIGHT) {
            // Update the light level value when sensor changes
            lightLevel = event.values[0] // Light level in lux

            // Update sine wave frequency based on light level
            // Light level will determine the frequency, mapping the lux value to a frequency range
            sineWaveFrequency = mapLightLevelToFrequency(lightLevel)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No need to handle accuracy changes for this example
    }

    // Map light level (lux) to a frequency (Hz), for example between 1 Hz to 1000 Hz
    private fun mapLightLevelToFrequency(lightLevel: Float): Double {
        // Assuming the light level range is from 0 to 1000 lux
        val minLux = 0f
        val maxLux = 1000f
        val minFreq = 1.0 // Minimum frequency in Hz
        val maxFreq = 1000.0 // Maximum frequency in Hz

        // Linearly map lux to frequency
        return minFreq + (maxFreq - minFreq) * (lightLevel - minLux) / (maxLux - minLux)
    }
}

@Composable
fun LightSensorUI(lightLevel: Float, sineWaveFrequency: Double) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Light Sensor",
            fontSize = 40.sp

        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Current Light Level: ${lightLevel} lux",

        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display the current frequency mapped from the light level
        Text(
            text = "Sine Wave Frequency: ${"%.2f".format(sineWaveFrequency)} Hz",

        )

        Spacer(modifier = Modifier.height(32.dp))

        // Sine wave graph
        SineWaveGraph(frequency = sineWaveFrequency, modifier = Modifier.fillMaxWidth().height(200.dp))
    }
}

@Composable
fun SineWaveGraph(frequency: Double, modifier: Modifier = Modifier) {
    // Generate a sine wave for the given frequency
    val samplesCount = 500
    val sampleRate = 1000
    val amplitude = 1f

    val sineWave = List(samplesCount) { i ->
        val time = i.toDouble() / sampleRate
        (amplitude * sin(2 * PI * frequency * time)).toFloat()
    }

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Normalize and draw the sine wave
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
