package com.example.project2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context?) {
        val lang = LanguagePreferences.getLanguage(newBase!!)
        val localized = LocaleHelper.setLocale(newBase, lang)
        super.attachBaseContext(localized)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HomeScreen(
                onLanguageChanged = { newLang ->
                    LanguagePreferences.saveLanguage(this, newLang)
                    recreate()
                }
            )
        }
    }
}

@Composable
fun HomeScreen(onLanguageChanged: (String) -> Unit) {

    val user = FirebaseAuth.getInstance().currentUser
    val email = user?.email ?: "Guest"
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFCCFFCC)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            LanguageToggle(onLanguageChanged = onLanguageChanged)

            Spacer(Modifier.height(20.dp))

            Image(
                painter = painterResource(id = R.drawable.image),
                contentDescription = "Banner",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = context.getString(R.string.homescreen_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0047AB)
            )

            Text(
                text = "🎡",
                style = MaterialTheme.typography.headlineLarge,
                color = Color(0xFF0047AB)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = context.getString(R.string.welcome_user, email),
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF0047AB)
            )

            Spacer(Modifier.height(32.dp))

            val buttonColors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0047AB),
                contentColor = Color.White
            )

            // Attractions button
            Button(
                onClick = {
                    context.startActivity(Intent(context, AttractionsListActivity::class.java))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = buttonColors
            ) {
                Text(context.getString(R.string.view_attractions))
            }

            Spacer(Modifier.height(16.dp))

            // Map button
            Button(
                onClick = {
                    context.startActivity(Intent(context, ParkMapActivity::class.java))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = buttonColors
            ) {
                Text(context.getString(R.string.map))
            }

            Spacer(Modifier.height(16.dp))

            // Favorites button
            Button(
                onClick = {
                    context.startActivity(Intent(context, FavoritesActivity::class.java))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = buttonColors
            ) {
                Text(context.getString(R.string.favorites))
            }
        }
    }
}

@Composable
fun LanguageToggle(onLanguageChanged: (String) -> Unit) {

    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0047AB),
                contentColor = Color.White
            )
        ) {
            Text("🌐 Language / زبان")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {

            DropdownMenuItem(
                text = { Text("English") },
                onClick = {
                    onLanguageChanged("en")
                    expanded = false
                }
            )

            DropdownMenuItem(
                text = { Text("فارسی") },
                onClick = {
                    onLanguageChanged("fa")
                    expanded = false
                }
            )
        }
    }
}
