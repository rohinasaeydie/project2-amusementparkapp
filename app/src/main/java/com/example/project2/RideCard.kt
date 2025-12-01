package com.example.project2

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
@Composable
fun RideCard(
    rideName: String,
    waitTime: Int,
    isOpen: Boolean,
    onSave: (() -> Unit)? = null,
    saveButtonText: String = LocalContext.current.getString(R.string.save_with_star)

) {
    val imageRes = rideImages[rideName] ?: rideImages["default"]!!

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = imageRes),
                contentDescription = rideName,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.height(12.dp))

            Text(
                rideName,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                "Status: ${if (isOpen) "Open" else "Closed"}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                "Wait: ${
                    if (!isOpen) "Closed"
                    else if (waitTime == 0) "No Wait"
                    else "$waitTime min"
                }",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(12.dp))

            if (onSave != null) {
                Button(onClick = onSave) {
                    Text(saveButtonText)
                }
            }
        }
    }
}
