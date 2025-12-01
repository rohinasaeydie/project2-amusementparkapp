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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FavoritesActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context?) {
        val lang = LanguagePreferences.getLanguage(newBase!!)
        val localized = LocaleHelper.setLocale(newBase, lang)
        super.attachBaseContext(localized)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { FavoritesScreen() }
    }
}@Composable
fun FavoritesScreen() {

    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser

    // If not logged in → show localized message
    val uid = user?.uid ?: return Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(context.getString(R.string.login_required))
    }

    var favorites by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    val database = FirebaseDatabase.getInstance().reference.child("favorites")

    // Load favorites from Firebase
    LaunchedEffect(uid) {
        database.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                favorites = snapshot.children.map {
                    (it.key ?: "") to (it.getValue(String::class.java) ?: "")
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // ⭐ Entire screen scrolls together
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFCCFFCC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ⭐ Centered title with emoji
        item {
            Text(
                text = "⭐ " + context.getString(R.string.favorites_title),
                style = MaterialTheme.typography.headlineLarge,
                color = Color(0xFF0047AB)
            )
        }

        // ⭐ Empty state
        if (favorites.isEmpty()) {
            item {
                Text(
                    text = context.getString(R.string.no_favorites),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF0047AB)
                )
            }
            return@LazyColumn
        }

        // ⭐ Favorite ride cards
        items(favorites) { (id, rideName) ->
            RideCard(
                rideName = rideName,
                waitTime = 0,
                isOpen = true,
                saveButtonText = context.getString(R.string.remove),
                onSave = {
                    database.child(uid).child(id).removeValue()
                    Toast.makeText(
                        context,
                        context.getString(R.string.remove),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }
}
