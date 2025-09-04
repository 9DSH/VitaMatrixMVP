package com.swings.vitamatrix.device

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.swings.vitamatrix.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ConnectionFragment : Fragment() {
    private lateinit var tvStatus: TextView
    private lateinit var tvDeviceInfo: TextView
    private lateinit var switchConnect: Button
    private var isConnected = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_wearable, container, false)
        tvStatus = view.findViewById(R.id.tvConnectionStatus)
        tvDeviceInfo = view.findViewById(R.id.tvDeviceInfo)
        switchConnect = view.findViewById(R.id.switchConnect)
        switchConnect.setOnClickListener { toggleConnection() }
        return view
    }

    private fun toggleConnection() {
        if (!isConnected) connectWearOS()
        else disconnectWearOS()
    }

    private fun connectWearOS() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            if (!isWearableApiAvailable()) {
                isConnected = false
                updateUIWithError("Wearable API not available on this device")
                return@launch
            }

            try {
                val nodes = Wearable.getNodeClient(requireContext()).connectedNodes.await()
                isConnected = nodes.isNotEmpty()
                val deviceNames = nodes.joinToString(", ") { it.displayName ?: it.id }
                updateUI(deviceNames)
            } catch (e: Exception) {
                isConnected = false
                updateUIWithError("Connection failed: ${e.message}")
            }
        }
    }

    private fun disconnectWearOS() {
        isConnected = false
        updateUI("-")
    }

    private fun isWearableApiAvailable(): Boolean {
        val result = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(requireContext())
        return result == ConnectionResult.SUCCESS
    }

    private fun updateUI(deviceInfo: String = "-") {
        requireActivity().runOnUiThread {
            if (isConnected) {
                tvStatus.text = "Wearable Connected"
                switchConnect.text = ""
            } else {
                tvStatus.text = "Wearable Disconnected"
                switchConnect.text = ""
            }
            tvDeviceInfo.text = "Connected Device: $deviceInfo"
        }
    }

    private fun updateUIWithError(message: String) {
        requireActivity().runOnUiThread {
            tvStatus.text = "Error: $message"
            tvDeviceInfo.text = "Connected Device: -"
            switchConnect.text = "Retry Connect"
        }
    }
}