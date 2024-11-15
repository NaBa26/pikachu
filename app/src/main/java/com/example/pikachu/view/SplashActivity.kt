package com.example.pikachu.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pikachu.ui.theme.PikachuTheme

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PikachuTheme {
                SplashScreenUI(
                    onNavigateToAccelerometer = { startMainActivity() },
                    onNavigateToGyroscope = { startGyroscopeActivity() },
                    onNavigateToLightSensor = { startLightSensorActivity() },
                    onNavigateToBarometerSensor = { startBarometerSensorActivity() },
                    onNavigateToInfraredSensor = { startInfraredSensorActivity() },
                    onNavigateToMagnetometer = { startMagnetometerActivity() }



                )
            }
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Finish SplashActivity so that it doesn't appear again when pressing back
    }

    private fun startGyroscopeActivity() {
        val intent = Intent(this, GyroscopeActivity::class.java)
        startActivity(intent)
        finish() // Finish SplashActivity so that it doesn't appear again when pressing back
    }
    private fun startLightSensorActivity() {
        val intent = Intent(this, LightSensorActivity::class.java)
        startActivity(intent)
        finish() // Finish SplashActivity so that it doesn't appear again when pressing back
    }
    private fun startBarometerSensorActivity() {
        val intent = Intent(this, BarometerActivity::class.java)
        startActivity(intent)
        finish() // Finish SplashActivity so that it doesn't appear again when pressing back
    }
    private fun startInfraredSensorActivity() {
        val intent = Intent(this, InfraredSensorActivity::class.java)
        startActivity(intent)
        finish() // Finish SplashActivity so that it doesn't appear again when pressing back
    }
    private fun startMagnetometerActivity() {
        val intent = Intent(this, MagnetometerActivity::class.java)
        startActivity(intent)
        finish() // Finish SplashActivity so that it doesn't appear again when pressing back
    }
}

@Composable
fun SplashScreenUI(
    onNavigateToAccelerometer: () -> Unit,
    onNavigateToGyroscope: () -> Unit,
    onNavigateToLightSensor: () -> Unit,
    onNavigateToBarometerSensor: ()-> Unit,
    onNavigateToInfraredSensor: ()-> Unit,
    onNavigateToMagnetometer: ()-> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BasicText(text = "Welcome to Pikachu App!", modifier = Modifier.padding(bottom = 16.dp))

        // Button to navigate to AccelerometerActivity
        Button(onClick = onNavigateToAccelerometer, modifier = Modifier.padding(bottom = 8.dp)) {
            Text("Accelerometer")
        }

        // Button to navigate to GyroscopeActivity
        Button(onClick = onNavigateToGyroscope) {
            Text("Gyroscope")
        }
        //Button to navigate to LightSensorActivity
        Button(onClick = onNavigateToLightSensor) {
            Text("Light Sensor")
        }
        Button(onClick = onNavigateToBarometerSensor) {
            Text("Barometer Sensor")
        }
        Button(onClick = onNavigateToInfraredSensor) {
            Text("Infrared Sensor")
        }
        Button(onClick = onNavigateToMagnetometer) {
            Text("Magnetometer")
        }
    }
}
