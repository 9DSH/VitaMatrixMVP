package com.swings.vitamatrix.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.swings.vitamatrix.R
import com.swings.vitamatrix.databinding.ActivityPatientDashboardBinding
import com.swings.vitamatrix.device.SensorService

class HomeDashboard : AppCompatActivity() {

    private lateinit var binding: ActivityPatientDashboardBinding
    private lateinit var navController: NavController

    // Request code for permissions
    private val PERMISSION_REQUEST_CODE = 1001

    // All permissions needed
    private val REQUIRED_PERMISSIONS = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACTIVITY_RECOGNITION,
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.INTERNET,
        Manifest.permission.FOREGROUND_SERVICE
    ).apply {
        // Add Bluetooth permissions if Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_CONNECT)
            add(Manifest.permission.BLUETOOTH_SCAN)
        }
        // Add Foreground Service Location permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.FOREGROUND_SERVICE_LOCATION)
        }
    }.toTypedArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityPatientDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = findNavController(R.id.nav_host_fragment_activity_patient_dashboard)
        setupSmoothBottomMenu()

        // Request permissions and start service after granted
        if (allPermissionsGranted()) {
            startSensorService()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE)
        }
    }

    private fun allPermissionsGranted(): Boolean {
        return REQUIRED_PERMISSIONS.all { perm ->
            ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun startSensorService() {
        val intent = Intent(this, SensorService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startSensorService()
            } else {
                Toast.makeText(
                    this,
                    "Required permissions not granted. The app may not work correctly.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupSmoothBottomMenu() {
        binding.bottomBar.setOnItemSelectedListener { index ->
            val destination = when (index) {
                0 -> R.id.navigation_questions
                1 -> R.id.navigation_wearable
                2 -> R.id.navigation_ai
                3 -> R.id.navigation_supplement
                else -> R.id.navigation_questions
            }
            navController.navigate(destination)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
