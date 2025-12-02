package com.example.project2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.project2.ui.theme.Project2Theme
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.launch

// ------------------------------------------------------------
// SignUpActivity: The screen where new users create accounts.
// Uses Firebase Authentication to create user credentials.
// ------------------------------------------------------------
class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        setContent {
            Project2Theme {
                SignUpScreen()
            }
        }
    }
}

@Composable
fun SignUpScreen() {

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Clear error when typing
    fun clearError() { error = null }

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

            Image(
                painter = painterResource(id = R.drawable.globe),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )

            Spacer(Modifier.height(20.dp))

            Text("Create Your Account", style = MaterialTheme.typography.headlineLarge, color = Color(0xFF0047AB))
            Text("📝", style = MaterialTheme.typography.headlineLarge, color = Color(0xFF0047AB))
            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; clearError() },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; clearError() },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; clearError() },
                        label = { Text("Confirm Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Live mismatch message
                    if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword) {
                        Text("Passwords do not match", color = Color.Red)
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (password != confirmPassword) {
                                error = "Passwords do not match."
                                return@Button
                            }

                            scope.launch {
                                isLoading = true
                                try {
                                    AuthRepository.register(email.trim(), password)
                                    Toast.makeText(context, "Account created!", Toast.LENGTH_SHORT).show()
                                    context.startActivity(Intent(context, MainActivity::class.java))
                                } catch (e: Exception) {
                                    error = friendlyAuthMessage(e, false)
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0047AB))
                    ) {
                        Text("Sign Up", color = Color.White)
                    }

                    error?.let {
                        Spacer(Modifier.height(12.dp))
                        Text(it, color = Color.Red)
                    }
                }
            }
        }
    }
}
