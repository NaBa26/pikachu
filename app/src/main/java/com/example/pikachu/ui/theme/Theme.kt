package com.example.pikachu.ui.theme

import android.app.Activity
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Define custom colors for red, blue, and black
private val RedColor = Color(0xFFFF0000) // Red color
private val BlueColor = Color(0xFF0000FF) // Blue color
private val BlackColor = Color(0xFF000000) // Black color

private val DarkColorScheme = darkColorScheme(
    primary = RedColor, // Red for primary
    secondary = BlueColor, // Blue for secondary
    background = BlackColor, // Black for background
    surface = BlackColor, // Black for surface
    onPrimary = Color.White, // White text on primary
    onSecondary = Color.White, // White text on secondary
    onBackground = Color.White, // White text on background
    onSurface = Color.White // White text on surface
)

private val LightColorScheme = lightColorScheme(
    primary = RedColor, // Red for primary
    secondary = BlueColor, // Blue for secondary
    background = BlackColor, // Black for background
    surface = BlackColor, // Black for surface
    onPrimary = Color.White, // White text on primary
    onSecondary = Color.White, // White text on secondary
    onBackground = Color.White, // White text on background
    onSurface = Color.White // White text on surface
)

@Composable
fun PikachuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Activity.SENSOR_SERVICE) as SensorManager }
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    var isLandscape = remember { false }

    // Sensor event listener for accelerometer data
    val sensorListener = rememberUpdatedState(object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null) {
                val x = event.values[0]
                val y = event.values[1]
                // Check if the device is in landscape or portrait mode based on accelerometer readings
                isLandscape = x > y // Example condition, adjust based on your needs
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    })

    // Registering and un-registering the sensor listener
    LaunchedEffect(Unit) {
        sensorManager.registerListener(sensorListener.value, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    // Define the color scheme based on the theme and device orientation
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Apply the theme based on the detected orientation (or any other criteria you choose)
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
