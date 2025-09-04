package com.swings.vitamatrix.device

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.swings.vitamatrix.R
import android.app.Service
import android.content.pm.ServiceInfo
import android.util.Log


class SensorService : LifecycleService(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var hrSensor: Sensor? = null
    private var currentSteps = 0
    private var currentHr = 0
    private var currentCalories = 0.0  // usually float/double
    private var currentBloodPressureSys = 0  // systolic
    private var currentBloodPressureDia = 0  // diastolic
    private var initialStepCount: Int? = null
    private var lastSentStepCount: Int = -1

    override fun onCreate() {
        super.onCreate()

        startForegroundService()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        hrSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

        if (stepSensor == null) {
            Log.w("SensorService", "Step counter sensor not available on this device.")
        } else {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

        if (hrSensor == null) {
            Log.w("SensorService", "Heart rate sensor not available on this device.")
        } else {
            sensorManager.registerListener(this, hrSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

        lifecycleScope.launch(Dispatchers.IO) {
            while (true) {
                // Send updated step count
                if (currentSteps != lastSentStepCount) {
                    sendData("/steps", currentSteps.toString())
                    lastSentStepCount = currentSteps
                }

                // Send heart rate
                sendData("/heartrate", currentHr.toString())

                // Simulate & send calories
                currentCalories = currentSteps * 0.04
                val caloriesString = String.format("%.1f", currentCalories)
                sendData("/calories", caloriesString)
                Log.d("SensorService", "Calories simulated: $caloriesString")

                // Simulate & send blood pressure
                currentBloodPressureSys = 120
                currentBloodPressureDia = 80
                val bpValue = "$currentBloodPressureSys/$currentBloodPressureDia"
                sendData("/bloodpressure", bpValue)
                Log.d("SensorService", "Blood pressure simulated: $bpValue")

                delay(30_000)
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val totalSteps = event.values[0].toInt()
                if (initialStepCount == null) {
                    initialStepCount = totalSteps
                    Log.d("SensorService", "Initial step count set: $initialStepCount")
                }
                currentSteps = totalSteps - (initialStepCount ?: 0)
                if (currentSteps < 0) currentSteps = 0  // just in case
                Log.d("SensorService", "Step count updated: $currentSteps")
            }
            Sensor.TYPE_HEART_RATE -> {
                currentHr = event.values[0].toInt()
                Log.d("SensorService", "Heart rate updated: $currentHr")
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun sendData(path: String, value: String) {
        Log.d("SensorService", "Preparing to send data - path: $path, value: $value")

        val request = PutDataMapRequest.create(path).apply {
            dataMap.putString("value", value)
        }.asPutDataRequest().setUrgent()

        Wearable.getDataClient(this).putDataItem(request)
            .addOnSuccessListener {
                Log.d("SensorService", "Data sent successfully: path=$path, value=$value")
            }
            .addOnFailureListener { e ->
                Log.e("SensorService", "Failed to send data: path=$path, value=$value", e)
            }
    }

    override fun onBind(intent: Intent): IBinder? = super.onBind(intent)

    private fun startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "sensor_service_channel"
            val channel = NotificationChannel(
                channelId,
                "Sensor Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)

            val notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle("Sensor Sync Running")
                .setContentText("Tracking steps and heart rate")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()

            startForeground(1, notification)
        } else {
            val notification = NotificationCompat.Builder(this)
                .setContentTitle("Sensor Sync Running")
                .setContentText("Tracking steps and heart rate")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()
            startForeground(1, notification)
        }
    }
}
