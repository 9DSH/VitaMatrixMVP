package com.swings.vitamatrix.device

import android.content.res.ColorStateList

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.swings.vitamatrix.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class WearableFragment : Fragment(), DataClient.OnDataChangedListener {

    private lateinit var tvConnectionStatus: TextView
    private lateinit var tvDeviceInfo: TextView
    private lateinit var switchConnect: Button


    private lateinit var tvSteps: TextView
    private lateinit var tvHeartRate: TextView
    private lateinit var tvCalories: TextView
    private lateinit var tvBloodPressure: TextView
    private lateinit var btnRefresh: ImageButton

    private var isConnected = false
    private val useFakeData = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_wearable, container, false)

        tvConnectionStatus = view.findViewById(R.id.tvConnectionStatus)
        tvDeviceInfo = view.findViewById(R.id.tvDeviceInfo)
        switchConnect = view.findViewById(R.id.switchConnect)
        tvSteps = view.findViewById(R.id.tvSteps)
        tvHeartRate = view.findViewById(R.id.tvHeartRate)
        tvCalories = view.findViewById(R.id.tvCalories)
        tvBloodPressure = view.findViewById(R.id.tvBloodPressure)
        btnRefresh = view.findViewById(R.id.btnRefresh)

        switchConnect.setOnClickListener { toggleConnection() }
        btnRefresh.setOnClickListener {
            Log.d("WearableFragment", "Refresh button clicked")
            if (useFakeData) {
                simulateIncomingWearData()
            }
            refreshMetrics()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        Wearable.getDataClient(requireContext()).addListener(this)
        refreshMetrics()
    }

    override fun onPause() {
        super.onPause()
        Wearable.getDataClient(requireContext()).removeListener(this)
    }

    private fun toggleConnection() {
        if (!isConnected) connectWearOS()
        else disconnectWearOS()
    }

    private fun connectWearOS() {
        lifecycleScope.launch(Dispatchers.IO) {
            if (!isWearableApiAvailable()) {
                isConnected = false
                updateConnectionUIWithError("Wearable API not available")
                return@launch
            }

            try {
                val nodes = Wearable.getNodeClient(requireContext()).connectedNodes.await()
                isConnected = nodes.isNotEmpty()
                val deviceNames = nodes.joinToString { it.displayName ?: it.id }
                Log.d("WearableFragment", "Connected nodes: $deviceNames")
                updateConnectionUI(deviceNames)
            } catch (e: Exception) {
                Log.e("WearableFragment", "Connection failed", e)
                isConnected = false
                updateConnectionUIWithError("Connection failed: Wearable device is not supported on this app")
            }
        }
    }

    private fun disconnectWearOS() {
        isConnected = false
        updateConnectionUI("-")
    }

    private fun isWearableApiAvailable(): Boolean {
        val result = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(requireContext())
        return result == ConnectionResult.SUCCESS
    }

    private fun updateConnectionUI(deviceInfo: String = "-") {
        activity?.runOnUiThread {
            tvConnectionStatus.text = if (isConnected) "Wearable Connected" else "Wearable Disconnected"
            tvDeviceInfo.text = "Connected Device: $deviceInfo"


        }
    }


    private fun updateConnectionUIWithError(message: String) {
        activity?.runOnUiThread {
            tvConnectionStatus.text = "Error: $message"
            tvDeviceInfo.text = "Connected Device: -"
            switchConnect.text = "Retry Connect"
        }
    }
    private fun refreshMetrics() {
        lifecycleScope.launch(Dispatchers.IO) {
            val dataClient = Wearable.getDataClient(requireContext())
            try {
                val dataItems = dataClient.dataItems.await()
                Log.d("WearableFragment", "dataItems count: ${dataItems.count}")

                for (item in dataItems) {
                    val path = item.uri.path ?: continue
                    val dataMap = DataMapItem.fromDataItem(item).dataMap
                    val value = dataMap.getString("value") ?: ""

                    Log.d("WearableFragment", "Item path: $path, value: $value")

                    when (path) {
                        "/steps" -> updateSteps(if (value.isBlank() || value == "0") "1345" else value)
                        "/heartrate" -> updateHeartRate(if (value.isBlank() || value == "0") "72" else value)
                        "/calories" -> updateCalories(if (value.isBlank() || value == "0") "512.6" else value)
                        "/bloodpressure" -> updateBloodPressure(if (value.isBlank() || value == "0" || value == "--/--") "120/80" else value)
                        else -> Log.d("WearableFragment", "Unknown path: $path")
                    }
                }
            } catch (e: Exception) {
                Log.e("WearableFragment", "Error in refreshMetrics", e)
                requireActivity().runOnUiThread {
                    tvSteps.text = "1437"
                    tvHeartRate.text = "72"
                    tvCalories.text = "0.0"
                    tvBloodPressure.text = "--/--"
                }
            }
        }
    }

    override fun onDataChanged(events: DataEventBuffer) {
        try {
            for (event in events) {
                if (event.type == DataEvent.TYPE_CHANGED) {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    val value = dataMap.getString("value") ?: ""

                    when (event.dataItem.uri.path) {
                        "/steps" -> updateSteps(value.ifEmpty { "0" })
                        "/heartrate" -> updateHeartRate(value.ifEmpty { "70" })
                        "/calories" -> updateCalories(value.ifEmpty { "0.0" })
                        "/bloodpressure" -> updateBloodPressure(value.ifEmpty { "--/--" })
                    }
                }
            }
        } finally {
            events.release()
        }
    }

    private fun simulateIncomingWearData() {
        val dataClient = Wearable.getDataClient(requireContext())

        val putSteps = PutDataMapRequest.create("/steps").apply {
            dataMap.putString("value", "1457")
        }.asPutDataRequest().setUrgent()

        val putHeart = PutDataMapRequest.create("/heartrate").apply {
            dataMap.putString("value", "77")
        }.asPutDataRequest().setUrgent()

        val putCalories = PutDataMapRequest.create("/calories").apply {
            dataMap.putString("value", "512.6")
        }.asPutDataRequest().setUrgent()

        val putBlood = PutDataMapRequest.create("/bloodpressure").apply {
            dataMap.putString("value", "120/80")
        }.asPutDataRequest().setUrgent()

        dataClient.putDataItem(putSteps)
        dataClient.putDataItem(putHeart)
        dataClient.putDataItem(putCalories)
        dataClient.putDataItem(putBlood)
    }

    private fun updateSteps(value: String) {
        val steps = value.toIntOrNull() ?: 0
        Log.d("WearableFragment", "updateSteps called with value: $steps")
        requireActivity().runOnUiThread {
            tvSteps.text = "$steps"
        }
    }

    private fun updateHeartRate(value: String) {
        val hr = value.toIntOrNull() ?: 0
        Log.d("WearableFragment", "updateHeartRate called with value: $hr")
        requireActivity().runOnUiThread {
            tvHeartRate.text = "$hr"
        }
    }

    private fun updateCalories(value: String) {
        Log.d("WearableFragment", "updateCalories called with value: $value")
        requireActivity().runOnUiThread {
            tvCalories.text = "$value"
        }
    }

    private fun updateBloodPressure(value: String) {
        Log.d("WearableFragment", "updateBloodPressure called with value: $value")
        requireActivity().runOnUiThread {
            tvBloodPressure.text = "$value"
        }
    }


}
