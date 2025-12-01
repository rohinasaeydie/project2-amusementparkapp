package com.example.project2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.google.firebase.auth.*

// --------------------------------------------------------
// MainActivity: Hosts the login screen for the application.
// Initializes Firebase and loads the LoginScreen composable.
// --------------------------------------------------------
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase before anything else uses it
        FirebaseApp.initializeApp(this)

        enableEdgeToEdge()

        setContent {
            Project2Theme {
                LoginScreen()
            }
        }
    }
}

@Composable
fun LoginScreen() {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // SharedPreferences hold saved login info when "Remember Me" is enabled.
    val prefs = context.getSharedPreferences("UniversalPrefs", Context.MODE_PRIVATE)

    // Load saved email/password if user checked "Remember Me" previously.
    var email by remember { mutableStateOf(prefs.getString("saved_email", "") ?: "") }
    var password by remember { mutableStateOf(prefs.getString("saved_password", "") ?: "") }
    var rememberMe by remember { mutableStateOf(prefs.getBoolean("remember_me", false)) }

    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // --------------------------------------------------------
    // Login screen UI starts here
    // --------------------------------------------------------
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFCCFFCC) // same pastel lime green theme
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Top banner image
            Image(
                painter = painterResource(id = R.drawable.globe),
                contentDescription = "Top Banner",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )

            Spacer(Modifier.height(18.dp))

            // App title shown above input fields
            Text(
                text = "Welcome to Universal Studios Guide 🌍",
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF0047AB)
            )

            Spacer(Modifier.height(24.dp))

            // Card containing the email + password fields
            Card(
                modifier = Modifier.fillMaxWidth(0.9f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth()
                ) {

                    // --------------------------------------------
                    // Email input field
                    // --------------------------------------------
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email", color = Color.White) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF5DADE2),
                            unfocusedBorderColor = Color(0xFF3498DB),
                            focusedContainerColor = Color(0xFF5DADE2),
                            unfocusedContainerColor = Color(0xFF3498DB),
                            cursorColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    // --------------------------------------------
                    // Password input field (hidden text)
                    // --------------------------------------------
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password", color = Color.White) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF5DADE2),
                            unfocusedBorderColor = Color(0xFF3498DB),
                            focusedContainerColor = Color(0xFF5DADE2),
                            unfocusedContainerColor = Color(0xFF3498DB),
                            cursorColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    // --------------------------------------------
                    // "Remember Me" toggle switch
                    // Saves login info only if user wants it
                    // --------------------------------------------
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Remember Me", color = Color(0xFF0047AB))
                        Switch(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it }
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // --------------------------------------------
                    // Login Button: Starts Firebase auth call
                    // --------------------------------------------
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                try {
                                    // Attempt to sign in through Firebase
                                    AuthRepository.login(email, password)

                                    Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()

                                    context.startActivity(Intent(context, HomeActivity::class.java))

                                    // Save credentials only if Remember Me is enabled
                                    with(prefs.edit()) {
                                        if (rememberMe) {
                                            putString("saved_email", email)
                                            putString("saved_password", password)
                                            putBoolean("remember_me", true)
                                        } else {
                                            // Clear stored data if user unchecks the switch
                                            remove("saved_email")
                                            remove("saved_password")
                                            putBoolean("remember_me", false)
                                        }
                                        apply()
                                    }

                                } catch (e: Exception) {
                                    // Convert Firebase error into friendly message for UI
                                    error = friendlyAuthMessage(e, true)
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = email.isNotBlank() && password.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0047AB),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Log In")
                    }

                    Spacer(Modifier.height(12.dp))

                    // Takes user to Registration screen
                    TextButton(
                        onClick = {
                            context.startActivity(Intent(context, SignUpActivity::class.java))
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Don't have an account? Create one", color = Color(0xFF0047AB))
                    }
                }
            }

            // Show loading spinner during Firebase authentication
            if (isLoading) {
                Spacer(Modifier.height(16.dp))
                CircularProgressIndicator(color = Color(0xFF0047AB))
            }

            // Show an error message if login failed
            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = Color.Red)
            }
        }
    }
}

// --------------------------------------------------------
// Converts Firebase authentication exceptions into user-
// friendly error messages instead of raw technical text.
// --------------------------------------------------------
fun friendlyAuthMessage(e: Exception, isLogin: Boolean): String {
    return when (e) {
        is FirebaseAuthInvalidUserException -> {
            if (isLogin) "We couldn’t find an account with that email. Try signing up."
            else "This email isn’t recognized."
        }
        is FirebaseAuthInvalidCredentialsException -> {
            if (isLogin) "Incorrect email or password. Please try again."
            else "That doesn’t look like a valid email or password."
        }
        is FirebaseAuthUserCollisionException -> {
            "An account with this email already exists. Try logging in."
        }
        is FirebaseAuthWeakPasswordException -> {
            "Password is too weak. Try at least 6–8 characters."
        }
        else -> {
            if (isLogin) "Couldn’t log in. Check your email/password or try later."
            else "Couldn’t create your account. Please try again."
        }
    }
}
