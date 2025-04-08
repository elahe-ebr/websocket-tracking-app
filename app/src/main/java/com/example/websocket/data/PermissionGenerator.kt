package com.example.websocket.data

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.example.websocket.app.MyApplication

object PermissionGenerator {

    fun locationPermissions(): MutableList<String> {
        return mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    fun checkLocationPermission(): Boolean {
        val accessFineLocationPermission = ContextCompat.checkSelfPermission(
            MyApplication.instance!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val accessCoarseLocationPermission = ContextCompat.checkSelfPermission(
            MyApplication.instance!!,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return accessFineLocationPermission == PackageManager.PERMISSION_GRANTED || accessCoarseLocationPermission == PackageManager.PERMISSION_GRANTED
    }

    fun Activity.isEnabledGPS(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }
}
