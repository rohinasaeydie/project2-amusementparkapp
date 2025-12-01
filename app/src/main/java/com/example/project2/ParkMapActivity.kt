package com.example.project2

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// ------------------------------------------------------------
// Apply the chosen language BEFORE the activity loads.
// This prevents context crashes and logout issues.
// ------------------------------------------------------------
class ParkMapActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context?) {
        val lang = LanguagePreferences.getLanguage(newBase!!)
        val localized = LocaleHelper.setLocale(newBase, lang)
        super.attachBaseContext(localized)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ParkMapScreen() }
    }
}

// ------------------------------------------------------------
// Data class representing rides, shows, events, etc.
// ------------------------------------------------------------
data class ParkRide(
    val id: String,
    val name: String,
    val waitTime: Int?,
    val status: String,
    val entityType: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)

// ------------------------------------------------------------
// Main MAP UI
// ------------------------------------------------------------
@Composable
fun ParkMapScreen() {

    val scope = rememberCoroutineScope()

    var items by remember { mutableStateOf<List<ParkRide>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Universal Orlando center point
    val universalCenter = LatLng(28.474321, -81.467819)

    // Camera (position + zoom)
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(universalCenter, 15f)
    }

    var zoom by remember { mutableStateOf(15f) }

    // Filter states: ALL, RIDES, SHOWS
    var selectedFilter by remember { mutableStateOf("ALL") }

    // ------------------------------------------------------------
    // Fetch park data + coordinates
    // ------------------------------------------------------------
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val liveList = fetchLiveRideData()
                val merged = mutableListOf<ParkRide>()

                for (item in liveList) {
                    val coords = fetchRideCoordinates(item.id)
                    if (coords != null) {
                        merged.add(
                            item.copy(
                                latitude = coords.first,
                                longitude = coords.second
                            )
                        )
                    }
                }

                items = merged
                loading = false

            } catch (e: Exception) {
                error = "Error loading park data."
                loading = false
            }
        }
    }

    // Apply filter
    val filteredItems = when (selectedFilter) {
        "RIDES" -> items.filter { it.entityType.equals("ATTRACTION", true) }
        "SHOWS" -> items.filter { it.entityType.equals("SHOW", true) }
        else -> items.filter {
            it.entityType.equals("ATTRACTION", true) ||
                    it.entityType.equals("SHOW", true)
        }
    }

    // ------------------------------------------------------------
    // UI Layout
    // ------------------------------------------------------------
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFCCFFCC)) // pastel lime
    ) {

        // Loading spinner
        if (loading) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
            return@Box
        }

        // Error message
        if (error != null) {
            Text(error!!, color = Color.Red, modifier = Modifier.align(Alignment.Center))
            return@Box
        }

        // ------------------------------------------------------------
        // The MAP itself
        // ------------------------------------------------------------
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraState,
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        ) {
            filteredItems.forEach { item ->
                val lat = item.latitude
                val lon = item.longitude

                if (lat != null && lon != null) {

                    // Marker color based on type
                    val hue = when (item.entityType.uppercase()) {
                        "ATTRACTION" -> BitmapDescriptorFactory.HUE_AZURE
                        "SHOW" -> BitmapDescriptorFactory.HUE_VIOLET
                        else -> BitmapDescriptorFactory.HUE_MAGENTA
                    }

                    Marker(
                        state = MarkerState(position = LatLng(lat, lon)),
                        title = item.name,
                        snippet = "Status: ${item.status}",
                        icon = BitmapDescriptorFactory.defaultMarker(hue)
                    )
                }
            }
        }

        // ------------------------------------------------------------
        // FILTER CHIPS
        // ------------------------------------------------------------
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(12.dp),
            colors = CardDefaults.cardColors(Color.White.copy(alpha = 0.9f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            LazyRow(
                modifier = Modifier.padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                val filters = listOf(
                    Triple("ALL", "✨", Color(0xFF99E699)),
                    Triple("RIDES", "🎢", Color(0xFF90CAF9)),
                    Triple("SHOWS", "🎭", Color(0xFFE1BEE7)),
                )

                filters.forEach { (key, emoji, color) ->
                    item {
                        AssistChip(
                            onClick = { selectedFilter = key },
                            label = { Text("$emoji $key") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor =
                                    if (selectedFilter == key) color
                                    else Color.White
                            )
                        )
                    }
                }
            }
        }

        // ------------------------------------------------------------
        // ZOOM + RECENTER Buttons
        // ------------------------------------------------------------
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            FloatingActionButton(
                onClick = {
                    cameraState.move(CameraUpdateFactory.newLatLngZoom(universalCenter, 15f))
                    zoom = 15f
                },
                containerColor = Color(0xFF0047AB),
                modifier = Modifier.size(55.dp)
            ) { Text("🎯", color = Color.White) }

            Spacer(Modifier.height(12.dp))

            FloatingActionButton(
                onClick = {
                    zoom = (zoom + 1).coerceAtMost(21f)
                    cameraState.move(
                        CameraUpdateFactory.newLatLngZoom(cameraState.position.target, zoom)
                    )
                },
                containerColor = Color(0xFF0047AB),
                modifier = Modifier.size(55.dp)
            ) { Text("+", color = Color.White) }

            Spacer(Modifier.height(12.dp))

            FloatingActionButton(
                onClick = {
                    zoom = (zoom - 1).coerceAtLeast(3f)
                    cameraState.move(
                        CameraUpdateFactory.newLatLngZoom(cameraState.position.target, zoom)
                    )
                },
                containerColor = Color(0xFF0047AB),
                modifier = Modifier.size(55.dp)
            ) { Text("-", color = Color.White) }
        }
    }
}

// ------------------------------------------------------------
// Network: Fetch live park ride data
// ------------------------------------------------------------
suspend fun fetchLiveRideData(): List<ParkRide> = withContext(Dispatchers.IO) {

    val url = URL("https://api.themeparks.wiki/v1/entity/89db5d43-c434-4097-b71f-f6869f495a22/live")
    val conn = url.openConnection() as HttpURLConnection

    val jsonText = conn.inputStream.bufferedReader().readText()
    conn.disconnect()

    val json = JSONObject(jsonText)
    val liveData = json.getJSONArray("liveData")
    val list = mutableListOf<ParkRide>()

    for (i in 0 until liveData.length()) {
        val obj = liveData.getJSONObject(i)

        val queueObj = obj.optJSONObject("queue")
        val standby = queueObj?.optJSONObject("STANDBY")
        val waitTime = standby?.optInt("waitTime")

        list.add(
            ParkRide(
                id = obj.getString("id"),
                name = obj.getString("name"),
                waitTime = waitTime,
                status = obj.optString("status", "UNKNOWN"),
                entityType = obj.optString("entityType", "UNKNOWN")
            )
        )
    }

    return@withContext list
}

// ------------------------------------------------------------
// Network: Fetch coordinates for each ride
// ------------------------------------------------------------
suspend fun fetchRideCoordinates(id: String): Pair<Double, Double>? =
    withContext(Dispatchers.IO) {

        val url = URL("https://api.themeparks.wiki/v1/entity/$id")
        val conn = url.openConnection() as HttpURLConnection

        val text = conn.inputStream.bufferedReader().readText()
        conn.disconnect()

        val json = JSONObject(text)
        if (!json.has("location")) return@withContext null

        val loc = json.getJSONObject("location")
        Pair(loc.getDouble("latitude"), loc.getDouble("longitude"))
    }
