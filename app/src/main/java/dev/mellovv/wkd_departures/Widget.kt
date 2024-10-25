package dev.mellovv.wkd_departures

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartService
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import dev.mellovv.wkd_departures.data.GlobalData
import dev.mellovv.wkd_departures.data.getTrainStationSchedule
import dev.mellovv.wkd_departures.data.stationIdToName

class MyAppWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MyAppWidget()
}


class MyAppWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            MyContent()
        }
    }

}


@Composable
fun MyContent() {
    val context = LocalContext.current
    val intent  = Intent(context, LocationService::class.java)

    Column(
        modifier = GlanceModifier.fillMaxSize().background(color = Color(0xFF121212)),
        verticalAlignment = Alignment.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (GlobalData.Trains.StationId == 0 && !GlobalData.Location.Loading && GlobalData.Location.Current == null) {
            Column(
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = GlanceModifier.fillMaxSize()
                    .clickable(
                        onClick = actionStartService<LocationService>(isForegroundService = false)
                    )

            ) {
                Image(
                    provider = ImageProvider(R.drawable.baseline_refresh_24),
                    contentDescription = "refresh",
                    colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
                    modifier = GlanceModifier.size(24.dp)
                )
                Text(
                    "Get nearest station schedule",
                    style = TextStyle(color = ColorProvider(Color.Gray), fontSize = 12.sp)
                )
            }
        } else if (GlobalData.Location.Loading || (GlobalData.Location.Current != null && GlobalData.Trains.StationId == 0)) {
            if (!GlobalData.Trains.Refreshing && GlobalData.Location.Current != null) {
                GlobalData.Trains.Refreshing = true
                getTrainStationSchedule(GlobalData.Location.Current!!)
            }
            Column(
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = GlanceModifier.fillMaxSize()
            ) {
                Text("Waiting", style = TextStyle(color = ColorProvider(Color.White)))
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = GlanceModifier.fillMaxSize()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = GlanceModifier.padding(horizontal = 9.dp, vertical = 6.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = stationIdToName(true, GlobalData.Trains.StationId)!!,
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        ),
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Image(
                        provider = ImageProvider(R.drawable.baseline_refresh_24),
                        contentDescription = "refresh",
                        colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
                        modifier = GlanceModifier.size(24.dp).clickable {
                            GlobalData.Trains.StationSchedule = List(10) { null }
                            GlobalData.Trains.StationId = 0
                            GlobalData.Location.Current = null
                            actionStartService(intent, isForegroundService = true)
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun ScheduleTile(direction: Int, departure: Int, delay: Int) {

}