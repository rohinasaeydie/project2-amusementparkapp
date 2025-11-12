package com.example.project2

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class Ride(
    val name: String,
    val waitTime: Int,
    val isOpen: Boolean
)

class RideListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { RideListScreen() }
    }
}

@Composable
fun RideListScreen() {
    var rides by remember { mutableStateOf<List<Ride>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    val email = user?.email ?: "Guest"
    val database = FirebaseDatabase.getInstance().reference

    // load data
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL("https://queue-times.com/parks/65/queue_times.json")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", "Mozilla/5.0 (Android UniversalGuide)")
                    connectTimeout = 7000
                    readTimeout = 7000
                }

                val body = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                conn.disconnect()

                val root = JSONObject(body)
                val list = mutableListOf<Ride>()

                val lands = root.getJSONArray("lands")
                for (i in 0 until lands.length()) {
                    val ridesArr = lands.getJSONObject(i).optJSONArray("rides") ?: JSONArray()
                    for (j in 0 until ridesArr.length()) {
                        val r = ridesArr.getJSONObject(j)
                        list.add(
                            Ride(
                                name = r.optString("name"),
                                waitTime = r.optInt("wait_time", 0),
                                isOpen = r.optBoolean("is_open", false)
                            )
                        )
                    }
                }
                val topRides = root.optJSONArray("rides") ?: JSONArray()
                for (i in 0 until topRides.length()) {
                    val r = topRides.getJSONObject(i)
                    list.add(
                        Ride(
                            name = r.optString("name"),
                            waitTime = r.optInt("wait_time", 0),
                            isOpen = r.optBoolean("is_open", false)
                        )
                    )
                }

                rides = list.sortedWith(compareByDescending<Ride> { it.isOpen }.thenBy { it.waitTime })
                if (rides.isEmpty()) error = "No rides found for Universal Studios."
            } catch (e: Exception) {
                error = "Could not load Universal Studios data."
            }
        }
    }

    // UI
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("🎢 Universal Studios Orlando", style = MaterialTheme.typography.titleLarge)
        Text("Logged in as: $email", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(12.dp))

        when {
            error != null -> Text(error ?: "", color = MaterialTheme.colorScheme.error)
            rides.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            else -> LazyColumn {
                items(rides) { ride ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(ride.name, style = MaterialTheme.typography.titleMedium)

                            val statusText = if (ride.isOpen) "Open" else "Closed"
                            val waitText =
                                if (!ride.isOpen) "Closed"
                                else if (ride.waitTime == 0) "No Wait"
                                else "${ride.waitTime} min"

                            Text(
                                "Status: $statusText | Wait: $waitText",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    if (user != null) {
                                        database.child("favorites")
                                            .child(user.uid)
                                            .push()
                                            .setValue(ride.name)
                                        Toast.makeText(
                                            context,
                                            "Saved ${ride.name} to favorites!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Please log in to save favorites.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("⭐ Save")
                            }
                        }
                    }
                }
            }
        }
    }
}