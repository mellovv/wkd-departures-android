package dev.mellovv.wkd_departures

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.Train
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import dev.mellovv.wkd_departures.data.GlobalData
import dev.mellovv.wkd_departures.screens.StationSchedule
import dev.mellovv.wkd_departures.ui.theme.Wkd_departuresTheme

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fun updateLocation() {
            GlobalData.Location.Loading = true

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        GlobalData.Location.Current = LatLng(latitude, longitude)
                        GlobalData.Location.Loading = false
                        GlobalData.Location.Error = false
                    } else {
                        GlobalData.Location.Error = true
                        GlobalData.Location.Loading = false
                    }
                }
                .addOnFailureListener {
                    GlobalData.Location.Error = true
                    GlobalData.Location.Loading = false
                }
        }

        GlobalData.Location.Request = { updateLocation() }

        enableEdgeToEdge()
        setContent {
            Wkd_departuresTheme {
                MainApp(this)
            }
        }
    }
}

@Composable
fun MainApp(context: Context) {
    val halfScreenWidth = LocalConfiguration.current.screenWidthDp / 2

    if (ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        GlobalData.Location.Granted = true
    } else {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                if (isGranted) {
                    GlobalData.Location.Granted = true
                    GlobalData.Location.AwaitingPermission = false
                } else {
                    GlobalData.Location.AwaitingPermission = false
                }
            })

        // Launch permission request
        if (!GlobalData.Location.Granted) {
            LaunchedEffect(Unit) {
                launcher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }
    }

    var showTrains by remember { mutableStateOf(true) }
    val animatedOffset by animateFloatAsState(
        targetValue = if (showTrains) 0f else 1f, label = "", animationSpec = tween(
            durationMillis = 200, easing = EaseOutBack
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFF121212))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFF090909))
                .padding(top = 45.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .clickable { showTrains = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Train,
                        "trains",
                        tint = if (showTrains) Color(0xFF00B2E8) else Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .clickable { showTrains = false },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.DirectionsBus,
                        "trains",
                        tint = if (!showTrains) Color(0xFF00B2E8) else Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            Spacer(Modifier.height(5.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = if (animatedOffset < 0) 0.dp else (animatedOffset * halfScreenWidth).dp)
            ) {
                Spacer(
                    modifier = Modifier
                        .height(2.dp)
                        .width(if (animatedOffset < 0) ((1f + animatedOffset) * halfScreenWidth).dp else halfScreenWidth.dp)
                        .background(color = Color(0xFF00B2E8))
                )
            }
        }
        StationSchedule(isTrain = true, stationsQuantity = 28, weight = if (showTrains) 1f else 0f)
        StationSchedule(isTrain = false, stationsQuantity = 0, weight = if (!showTrains) 1f else 0f)
    }


}