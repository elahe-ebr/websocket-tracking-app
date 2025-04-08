package com.example.websocket.ui

import android.Manifest
import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.websocket.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.websocket.R
import com.example.websocket.data.PermissionGenerator
import com.example.websocket.data.PermissionGenerator.isEnabledGPS
import com.example.websocket.service.LocationService
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val mViewModel: MainViewModel by viewModels()

    private val requestGpsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val accessFineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val accessCoarseGranted =
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            if (accessFineGranted && accessCoarseGranted) checkEnableLocation()
            else
                Toast.makeText(this, "Grant location permission to continue", Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        init()
    }

    private fun init() {
        onclick()
        observe()
    }

    private fun onclick() {
        binding.btnStartService.setOnClickListener {
            checkPermissions()
            binding.etMessage.text.clear()
        }

        binding.btnSendMessage.setOnClickListener {
            if (isServiceRunning(LocationService::class.java, this))
                stopLocationService()
            mViewModel.sendMessage(binding.etMessage.text.toString())
            binding.etMessage.text.clear()
        }

        binding.btnStopService.setOnClickListener {
            if (isServiceRunning(LocationService::class.java, this))
                stopLocationService()
            else
                Toast.makeText(this, "The service is currently not active", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermissions() {
        if (PermissionGenerator.checkLocationPermission())
            checkEnableLocation()
        else
            requestGpsPermissionLauncher.launch(
                PermissionGenerator.locationPermissions().toTypedArray()
            )
    }

    private fun checkEnableLocation() {
        if (!this.isEnabledGPS())
            Toast.makeText(
                this,
                "Your location is not enabled. Please enable it to find your location",
                Toast.LENGTH_SHORT
            ).show()
        else
            startLocationService()
    }

    private fun isServiceRunning(serviceClass: Class<out Service>, context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className)
                return true
        }
        return false
    }

    private fun startLocationService() {
        lifecycleScope.launch {
            if (!isServiceRunning(LocationService::class.java, this@MainActivity)) {
                val intent = Intent(
                    this@MainActivity,
                    LocationService::class.java
                )
                this@MainActivity.startService(intent)
                Toast.makeText(this@MainActivity, "Start location service", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun observe() {
        lifecycleScope.launch {
            mViewModel.messages.collect {
                binding.tvResponse.text = it
            }
        }
    }

    private fun stopLocationService() {
        val intent = Intent(this, LocationService::class.java)
        this.stopService(intent)
        Toast.makeText(this@MainActivity, "Stop location service", Toast.LENGTH_SHORT)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isServiceRunning(LocationService::class.java, this))
            stopLocationService()
    }
}