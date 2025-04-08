package com.example.websocket.service

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.HandlerThread
import android.os.IBinder
import androidx.core.app.ActivityCompat
import com.example.websocket.app.MyApplication
import com.example.websocket.data.PermissionGenerator
import com.example.websocket.data.WebSocketManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationService : Service() {

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var handlerThread: HandlerThread? = null

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!checkPermissions()) {
            stopSelf()
            return START_NOT_STICKY
        }

        CoroutineScope(Dispatchers.IO).launch {
            getLocation()
        }

        return START_STICKY
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                MyApplication.instance!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                MyApplication.instance!!,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permissions not granted, handle appropriately
            return
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateDistanceMeters(0f)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val lastLocation = locationResult.lastLocation

                lastLocation?.let { location ->
                    sendLocationToServer(location)
                }
            }
        }

        handlerThread = HandlerThread("LocationHandlerThread")
        handlerThread!!.start()
        val looper = handlerThread!!.looper

        fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback!!, looper)
    }

    private fun sendLocationToServer(location: Location) {
        val timestamp = System.currentTimeMillis()
        val message = "lat=${location.latitude}, lng=${location.longitude}, time=$timestamp"
        WebSocketManager.sendMessage(message)
    }

    private fun checkPermissions(): Boolean {
        return PermissionGenerator.checkLocationPermission()
    }


    override fun onDestroy() {
        super.onDestroy()
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient!!.removeLocationUpdates(locationCallback!!)
            CoroutineScope(Dispatchers.IO).launch {
                handlerThread?.quitSafely()
            }
        }
        locationCallback = null
        fusedLocationClient = null
    }

}