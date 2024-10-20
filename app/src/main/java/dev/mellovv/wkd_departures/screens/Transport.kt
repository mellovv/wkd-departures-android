package dev.mellovv.wkd_departures.screens

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.OverscrollConfiguration
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.runtime.Composable
import dev.mellovv.wkd_departures.data.GlobalData
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mellovv.wkd_departures.data.getTrainStationSchedule
import dev.mellovv.wkd_departures.data.trainStationIdToName
import dev.mellovv.wkd_departures.modules.LoadingGlyph
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun StationSchedule(isTrain: Boolean, stationsQuantity: Int, weight: Float) {
    var showingVehicleSchedule by remember { mutableStateOf(false) }
    var stationSheetVisible by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val stationId = if (isTrain) GlobalData.Trains.StationId else GlobalData.Buses.StationId
    val stationSchedule =
        if (isTrain) GlobalData.Trains.StationSchedule else GlobalData.Buses.StationSchedule
    val vehicleNumber =
        if (isTrain) GlobalData.Trains.VehicleNumber else GlobalData.Buses.VehicleNumber
    val vehicleSchedule =
        if (isTrain) GlobalData.Trains.VehicleSchedule else GlobalData.Buses.VehicleSchedule

    if (stationId == 0 && GlobalData.Location.Granted && !GlobalData.Location.Loading && GlobalData.Location.Current == null && !GlobalData.Location.Error) {
        GlobalData.Location.Request()
    }

    if (GlobalData.Location.Granted && !GlobalData.Location.Loading && GlobalData.Location.Current != null && stationId == 0) {
        if (isTrain) getTrainStationSchedule(GlobalData.Location.Current!!)
        else {
            // TODO bus api
        }
    }

    if (!GlobalData.Location.Granted && !GlobalData.Location.AwaitingPermission) {
        if (isTrain) getTrainStationSchedule(1)
        else {
            // TODO bus api
        }

    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = if (isTrain) GlobalData.Trains.Refreshing else GlobalData.Buses.Refreshing,
        onRefresh = {
            if (stationId != 0 && stationSchedule[0] != null) {
                if (isTrain) {
                    GlobalData.Trains.Refreshing = true
                    getTrainStationSchedule(GlobalData.Trains.StationId)
                } else {
                    // TODO bus api
                }
            }
        }
    )


    Column(modifier = Modifier.fillMaxSize(weight)) {
        if (!showingVehicleSchedule) Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFF090909))
                .padding(horizontal = 15.dp)
                .height(60.dp)
        ) {
            val canChangeStations =
                !(stationId == 0 || (stationSchedule[0] == null))

            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(enabled = canChangeStations) {
                    stationSheetVisible = true
                }) {
                Icon(
                    Icons.Rounded.ArrowDropDown,
                    "Change station",
                    tint = if (canChangeStations) Color.White
                    else Color.Gray,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(Modifier.width(5.dp))
                if (canChangeStations) {
                    Text(
                        text = if (isTrain) trainStationIdToName(GlobalData.Trains.StationId)
                        else "Bus",
                        color = Color.White,
                        fontSize = 16.sp,
                        letterSpacing = (-0.5).sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.width(200.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .height(40.dp)
                            .clip(RoundedCornerShape(5.dp))
                    ) {
                        LoadingGlyph()
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.LocationOn,
                    "Find nearest",
                    tint = if (GlobalData.Location.Granted) Color.White else Color.Gray,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        else Row {}
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(0xFF121212)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    stationSchedule.forEach {
                        ScheduleTile(
                            true, isTrain, it?.direction, it?.number, it?.departure, it?.delay
                        )
                    }
                }
                if (stationId != 0 && stationSchedule[0] != null) {
                    PullRefreshIndicator(
                        refreshing = if (isTrain) GlobalData.Trains.Refreshing else GlobalData.Buses.Refreshing,
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter),
                        backgroundColor = Color(0xFF121212),
                        contentColor = Color.White
                    )
                }
            }
        }
    }
    if (stationSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { stationSheetVisible = false }, sheetState = sheetState,
            containerColor = Color(0xFF121212),
            dragHandle = {},
        ) {
            StationPicker(true, stationsQuantity)
        }
    }
}

@Composable
fun ScheduleTile(
    isVehicle: Boolean,
    isTrain: Boolean,
    direction: Int?,
    number: Int?,
    departure: Int?,
    delay: Int?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(7.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .background(color = Color(0xFF1D1D1D), shape = RoundedCornerShape(10.dp))
                .clip(shape = RoundedCornerShape(10.dp))
        ) {
            if (direction != null && number != null && departure != null && delay != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 15.dp)
                ) {
                    Text(
                        trainStationIdToName(direction),
                        fontSize = 19.sp,
                        color = Color.White,
                        modifier = Modifier.width(220.dp),
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.7).sp
                    )

                    val now = LocalDateTime.now()
                    var nowTime = now.hour * 60 + now.minute
                    if (departure < 60) nowTime -= 1440
                    var text = ""
                    if (nowTime == 0) text = "Odjazd"
                    else if (departure - nowTime < 60 && isVehicle) text =
                        "${departure - nowTime} min"
                    else text =
                        "${departure / 60}:${if (departure % 60 >= 10) departure % 60 else "0${departure % 60}"}"
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (delay > 0) {
                            Text(
                                "",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                            )
                        }
                        Text(
                            text = text,
                            fontSize = 25.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-1).sp
                        )
                        if (delay > 0) {
                            Text(
                                "($delay min)",
                                color = Color.Red,
                                fontWeight = FontWeight.Light,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                LoadingGlyph()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StationPicker(isTrain: Boolean, stationsQuantity: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .height(670.dp)
            .fillMaxWidth()
            .padding(horizontal = 5.dp)
    ) {
        Text(
            "Wybierz stację",
            fontSize = 20.sp,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.5).sp,
            modifier = Modifier.padding(vertical = 10.dp)
        )
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .overscroll(ScrollableDefaults.overscrollEffect())
        ) {
            for (id in 1..stationsQuantity) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(
                            color = Color(0xFF1D1D1D), shape = RoundedCornerShape(10.dp)
                        )
                ) {
                    val text = if (isTrain) trainStationIdToName(id)
                    else "Bus"
                    Text(
                        text,
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = (-0.5).sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun StationTilePreview() {
//    ScheduleTile(false, true, 1, 217, 750, 5)
    StationSchedule(false, 28, 1f)
}
