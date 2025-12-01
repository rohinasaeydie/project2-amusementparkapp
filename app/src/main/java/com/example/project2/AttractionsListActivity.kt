package com.example.project2

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

class AttractionsListActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context?) {
        val lang = LanguagePreferences.getLanguage(newBase!!)
        val localized = LocaleHelper.setLocale(newBase, lang)
        super.attachBaseContext(localized)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AttractionsListScreen() }
    }
}@Composable
fun AttractionsListScreen() {

    var rides by remember { mutableStateOf<List<Ride>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    val database = FirebaseDatabase.getInstance().reference

    // ⭐ Load API data
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL("https://queue-times.com/parks/65/queue_times.json")
                val conn = url.openConnection() as HttpURLConnection

                conn.requestMethod = "GET"
                val body = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }

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
                                waitTime = r.optInt("wait_time"),
                                isOpen = r.optBoolean("is_open")
                            )
                        )
                    }
                }

                rides = list

            } catch (e: Exception) {
                error = "Error loading ride data"
            }
        }
    }

    // ⭐ FULL SCREEN SCROLL — EVERYTHING IN ONE SCROLLER
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFCCFFCC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 48.dp)
    ) {

        // ⭐ EMOJI TITLE — THEMED & CENTERED
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎢 " + context.getString(R.string.attractions_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFF0047AB)
                )
            }
        }

        // ⭐ Error message
        if (error != null) {
            item {
                Text(
                    text = error!!,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            return@LazyColumn
        }

        // ⭐ Ride list — scrollable now
        items(rides) { ride ->

            RideCard(
                rideName = ride.name,
                waitTime = ride.waitTime,
                isOpen = ride.isOpen,
                onSave = {
                    if (user != null) {

                        val key = ride.name.replace(".", "_")

                        database.child("favorites")
                            .child(user.uid)
                            .child(key)
                            .get()
                            .addOnSuccessListener { snap ->
                                if (snap.exists()) {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.already_saved),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    snap.ref.setValue(ride.name)
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.saved),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.login_required),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }
    }
}
