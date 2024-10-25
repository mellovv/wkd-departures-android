package dev.mellovv.wkd_departures

import android.Manifest
import android.annotation.SuppressLint
import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import dev.mellovv.wkd_departures.data.GlobalData

@Suppress("DEPRECATION")
class LocationService : IntentService(LocationService::class.simpleName) {
    @Deprecated("Deprecated in Java")
    override fun onHandleIntent(workIntent: Intent?
    ) {
        GlobalData.Location.Loading = true
        val fusedLocationClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("log", "Location permission not granted")
            GlobalData.Location.Granted = false
            GlobalData.Location.Loading = false
            return
        } else {
            Log.d("log", "Location permission granted")
        }


        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000 // Request location every second
            fastestInterval = 500
            numUpdates = 1
        }


        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                Log.d("log", "Callback triggered")
                for (location in locationResult.locations) {
                    if (location != null) {
                        Log.d("log", "location: ${location.latitude}, ${location.longitude}")
                    } else {
                        Log.d("log", "location is null")
                    }
                }
                fusedLocationClient.removeLocationUpdates(this).addOnFailureListener {
                    Log.d("log", it.message.toString())
                }
            }

        }

        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            Log.d("log", "Google Play Services not available")
            return
        }

        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d("log", "GPS is disabled")
            return
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        Log.d("log", "location requested")
    }
}