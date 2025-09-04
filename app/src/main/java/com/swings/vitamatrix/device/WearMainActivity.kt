package com.swings.vitamatrix.device

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity

class WearMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, SensorService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        finish() // No Compose UI — exit after starting service
    }
}
