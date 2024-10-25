package dev.mellovv.wkd_departures.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.OverscrollConfiguration
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.runtime.Composable
import dev.mellovv.wkd_departures.data.GlobalData
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ChevronLeft
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mellovv.wkd_departures.data.getTrainSchedule
import dev.mellovv.wkd_departures.data.getTrainStationSchedule
import dev.mellovv.wkd_departures.data.stationIdToName
import dev.mellovv.wkd_departures.modules.LoadingGlyph
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import kotlin.math.max

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun StationSchedule(isTrain: Boolean, stationsQuantity: Int, weight: Float) {
    val coroutineScope = rememberCoroutineScope()

    if (!isTrain) {
        Column(
            modifier = Modifier.fillMaxSize(weight),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Porzucone", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        return
    }

    var showingVehicleSchedule by remember { mutableStateOf(false) }
    var stationSheetVisible by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var enlightenedIndex by remember { mutableIntStateOf(-1) }

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
                    if (showingVehicleSchedule) getTrainSchedule(vehicleNumber)
                    else getTrainStationSchedule(GlobalData.Trains.StationId)
                } else {
                    // TODO bus api
                }
            }
        }
    )

    val lazyListState = rememberLazyListState()
    var scrolled by remember { mutableStateOf(false) }

    if (showingVehicleSchedule && !scrolled && vehicleSchedule[0] != null) {
        vehicleSchedule.forEachIndexed { i, it ->
            if (it?.id == stationId) {
                enlightenedIndex = i
            }
        }
        scrolled = true
        coroutineScope.launch {
            lazyListState.scrollToItem(max(enlightenedIndex - 1, 0))
        }
    }

    Log.d("index", enlightenedIndex.toString())

    Column(modifier = Modifier.fillMaxSize(weight)) {
        BackHandler(enabled = showingVehicleSchedule) {
            showingVehicleSchedule = false
            scrolled = false
            GlobalData.Trains.VehicleSchedule = List(10) { null }
            GlobalData.Trains.VehicleNumber = 0
            enlightenedIndex = -1
            coroutineScope.launch {
                lazyListState.scrollToItem(0)
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFF090909))
                .padding(horizontal = 15.dp)
                .height(60.dp)
        ) {
            if (!showingVehicleSchedule) {
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
                    if (stationId != 0) {
                        Text(
                            text = stationIdToName(isTrain, GlobalData.Trains.StationId)!!,
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
                        modifier = Modifier
                            .size(30.dp)
                            .clickable {
                                if (isTrain) {
                                    GlobalData.Trains.StationId = 0
                                    GlobalData.Trains.StationSchedule = List(10) { null }
                                } else {
                                    GlobalData.Buses.StationId = 0
                                    GlobalData.Buses.StationSchedule = List(10) { null }
                                }
                                GlobalData.Location.Request()
                            }
                    )
                }
            } else {
                Icon(
                    Icons.Rounded.ArrowBackIosNew,
                    "Go back",
                    tint = if (GlobalData.Location.Granted) Color.White else Color.Gray,
                    modifier = Modifier
                        .size(25.dp)
                        .clickable {
                            showingVehicleSchedule = false
                            scrolled = false
                            GlobalData.Trains.VehicleSchedule = List(10) { null }
                            GlobalData.Trains.VehicleNumber = 0
                            enlightenedIndex = -1
                            coroutineScope.launch {
                                lazyListState.scrollToItem(0)
                            }
                        }
                )
                val text = if (isTrain) "Wkd nr. $vehicleNumber" else "Bus"
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 18.sp,
                    letterSpacing = (-0.5).sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Icon(
                    Icons.Rounded.ArrowBackIosNew,
                    "Spacer",
                    tint = Color.Transparent,
                    modifier = Modifier.size(25.dp)
                )
            }
        }
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

                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    items(if (showingVehicleSchedule) vehicleSchedule.size else stationSchedule.size) {
                        if (showingVehicleSchedule) {
                            vehicleSchedule[it].let { station ->
                                ScheduleTile(
                                    isVehicle = false,
                                    isTrain = isTrain,
                                    name = stationIdToName(isTrain, station?.id),
                                    number = station?.id,
                                    departure = station?.departure,
                                    enlightened = it == enlightenedIndex,
                                    delay = station?.delay
                                )
                            }
                        } else {
                            stationSchedule[it].let { vehicle ->
                                ScheduleTile(
                                    isVehicle = true,
                                    isTrain = isTrain,
                                    name = stationIdToName(isTrain, vehicle?.direction),
                                    number = vehicle?.number,
                                    departure = vehicle?.departure,
                                    delay = vehicle?.delay,
                                    enlightened = it == enlightenedIndex,
                                    modifier = Modifier.clickable {
                                        showingVehicleSchedule = true
                                        GlobalData.Trains.VehicleNumber = vehicle?.number!!
                                        getTrainSchedule(vehicle.number)
                                    }
                                )
                            }
                        }
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
            onDismissRequest = { stationSheetVisible = false },
            sheetState = sheetState,
            containerColor = Color(0xFF121212),
            dragHandle = {},
        ) {
            StationPicker(true, stationsQuantity) { stationSheetVisible = false }
        }
    }
}

@Composable
fun ScheduleTile(
    isVehicle: Boolean,
    isTrain: Boolean,
    name: String?,
    number: Int?,
    departure: Int?,
    delay: Int?,
    enlightened: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(7.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .background(
                    color = Color(if (enlightened) 0xFF213131 else 0xFF1D1D1D),
                    shape = RoundedCornerShape(10.dp)
                )
                .clip(shape = RoundedCornerShape(10.dp))
        ) {
            if (name != null && number != null && departure != null && delay != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 15.dp)
                ) {
                    Text(
                        text = name,
                        fontSize = 19.sp,
                        color = Color.White,
                        modifier = Modifier.width(220.dp),
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.7).sp
                    )

                    val now = LocalDateTime.now()
                    var nowTime = now.hour * 60 + now.minute
                    if (departure < 60 && nowTime > 1380) nowTime -= 1440
                    var text = ""
                    text = if (departure - nowTime == 0) "Odjazd"
                    else if (departure - nowTime < 0) "Odjechał"
                    else if (departure - nowTime < 60 && isVehicle) "${departure - nowTime} min"
                    else "${departure / 60}:${if (departure % 60 >= 10) departure % 60 else "0${departure % 60}"}"
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
                        if (delay != 0) {
                            Text(
                                text = "(${if (delay > 0) "+" else ""}$delay min)",
                                color = if (delay > 0) Color.Red else Color.Green,
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
fun StationPicker(isTrain: Boolean, stationsQuantity: Int, hideModal: () -> Unit) {
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
                        .clickable {
                            GlobalData.Trains.StationId = id
                            GlobalData.Trains.StationSchedule = List(10) { null }
                            getTrainStationSchedule(id)
                            hideModal()

                        }
                ) {
                    Text(
                        text = stationIdToName(isTrain, id)!!,
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
