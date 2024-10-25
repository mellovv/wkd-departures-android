package dev.mellovv.wkd_departures.data

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.maps.model.LatLng

object GlobalData {
    object Trains {
        var StationId by mutableIntStateOf(0)
        var StationSchedule by mutableStateOf(List(10) { null } as List<Vehicle?>)
        var VehicleNumber by mutableIntStateOf(0)
        var VehicleSchedule by mutableStateOf(List(10) { null } as List<Station?>)
        var Refreshing by mutableStateOf(false)
    }

    object Buses {
        var StationId by mutableIntStateOf(0)
        var StationSchedule by mutableStateOf(List(10) { null } as List<Vehicle?>)
        var VehicleNumber by mutableIntStateOf(0)
        var VehicleSchedule by mutableStateOf(List(10) { null } as List<Station?>)
        var Refreshing by mutableStateOf(false)
    }

    object Location {
        var Granted: Boolean = false
        var Error: Boolean = false
        var Current by mutableStateOf(null as LatLng?)
        var Loading by mutableStateOf(false)
        var Request: () -> Unit = {}
        var AwaitingPermission by mutableStateOf(true)
    }
}