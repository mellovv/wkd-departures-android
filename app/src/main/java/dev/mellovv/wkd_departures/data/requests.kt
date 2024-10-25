package dev.mellovv.wkd_departures.data

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.stream.IntStream.range
import kotlin.random.Random

object Routes {
    private const val HOST: String = "https://wkd-departures.vercel.app/api"
    const val TRAIN_SCHEDULE: String = "$HOST/train"
    const val STATION_SCHEDULE: String = "$HOST/departures"
}


val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()

fun postFetch(jsonBody: String, url: String, callback: Callback) {
    val body: RequestBody = jsonBody.toRequestBody(JSON)

    val client = OkHttpClient()
    val request: Request = Request.Builder().url(url).post(body).build()
    val call: Call = client.newCall(request)

    call.enqueue(callback)
}

fun getTrainSchedule(trainNumber: Int) {
    val jsonObject = JSONObject()
    jsonObject.put("trainNumber", trainNumber)
    val jsonBody = jsonObject.toString()

    postFetch(jsonBody, Routes.TRAIN_SCHEDULE, object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            // TODO error handling
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.body == null) {
                // TODO error handling
                return
            }
            val responseJson = JSONArray(response.body!!.string())

            val trainSchedule: MutableList<Station> = mutableListOf()
            for (i in range(0, responseJson.length())) {
                val station = responseJson.getJSONObject(i)
                val stationId = station.getInt("id")
                val departure = station.getInt("departure")
                val delay = station.getInt("delay")
                trainSchedule += Station(stationId, departure, delay)
            }

            GlobalData.Trains.VehicleSchedule = trainSchedule
            GlobalData.Trains.Refreshing = false
        }
    })


}

fun getTrainStationSchedule(jsonObject: JSONObject) {
    val jsonBody = jsonObject.toString()


    postFetch(jsonBody, Routes.STATION_SCHEDULE, object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            // TODO error handling
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.body == null) {
                // TODO error handling
                return
            }
            val responseJson = JSONArray(response.body!!.string())

            var stationId = 0
            val stationSchedule: MutableList<Vehicle> = mutableListOf()
            for (i in range(0, responseJson.length())) {
                val train = responseJson.getJSONObject(i)
                val number = train.getInt("number")
                val direction = train.getInt("direction")
                val departure = train.getInt("departure")
                val delay = train.getInt("delay")
                stationId = train.getInt("stationId")
                Log.d("departure", departure.toString())
                stationSchedule += Vehicle(number, direction, departure, delay)
            }
            GlobalData.Trains.StationId = stationId
            GlobalData.Trains.StationSchedule = stationSchedule
            GlobalData.Trains.Refreshing = false
        }
    })
}

fun getTrainStationSchedule(stationId: Int) {
    val jsonObject = JSONObject()
    jsonObject.put("stationId", stationId)
    getTrainStationSchedule(jsonObject)
}


fun getTrainStationSchedule(location: LatLng) {
    val jsonObject = JSONObject()
    jsonObject.put("lat", location.latitude)
    jsonObject.put("lon", location.longitude)
    getTrainStationSchedule(jsonObject)
}
