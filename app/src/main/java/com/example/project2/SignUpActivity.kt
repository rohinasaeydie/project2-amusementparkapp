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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.project2.ui.theme.Project2Theme
import kotlinx.coroutines.launch

// ------------------------------------------------------------
// SignUpActivity: The screen where new users create accounts.
// Uses Firebase Authentication to create user credentials.
// ------------------------------------------------------------
class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
    val context = androidx.compose.ui.platform.LocalContext.current

    // Local state variables that hold the user's input
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // UI feedback values
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // ------------------------------------------------------------
    // Fullscreen background for the Signup page
    // ------------------------------------------------------------
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFCCFFCC) // same pastel lime-green theme as the rest of the app
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Large banner image at the top of the signup screen
            Image(
                painter = painterResource(id = R.drawable.globe),
                contentDescription = "Banner",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )

            Spacer(Modifier.height(20.dp))

            // Title + emoji centered under the banner
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Create Your Account",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFF0047AB)
                )
                Text(
                    text = "📝",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFF0047AB)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Subtitle describing what the account is for
            Text(
                text = "Join Universal Studios Guide!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF0047AB)
            )

            Spacer(Modifier.height(32.dp))

            // ------------------------------------------------------------
            // Signup form container
            // ------------------------------------------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    // -------------------------------
                    // Email Input Field
                    // -------------------------------
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email", color = Color.White) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF5DADE2),
                            unfocusedBorderColor = Color(0xFF3498DB),
                            focusedLabelColor = Color.White,
                            cursorColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF5DADE2),
                            unfocusedContainerColor = Color(0xFF3498DB)
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    // -------------------------------
                    // Password Input Field
                    // -------------------------------
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
                            focusedLabelColor = Color.White,
                            cursorColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF5DADE2),
                            unfocusedContainerColor = Color(0xFF3498DB)
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    // -------------------------------
                    // Confirm Password Field
                    // Ensures user typed the same password twice
                    // -------------------------------
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password", color = Color.White) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF5DADE2),
                            unfocusedBorderColor = Color(0xFF3498DB),
                            focusedLabelColor = Color.White,
                            cursorColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF5DADE2),
                            unfocusedContainerColor = Color(0xFF3498DB)
                        )
                    )

                    Spacer(Modifier.height(24.dp))

                    // ------------------------------------------------------------
                    // Sign Up Button
                    // Creates a Firebase user if passwords match
                    // ------------------------------------------------------------
                    Button(
                        onClick = {
                            // Basic check before calling Firebase
                            if (password != confirmPassword) {
                                error = "Passwords do not match."
                                return@Button
                            }

                            // Firebase auth call
                            scope.launch {
                                isLoading = true
                                try {
                                    AuthRepository.register(email, password)

                                    Toast.makeText(context, "Account created!", Toast.LENGTH_SHORT).show()

                                    // After creating the account, return user to login screen
                                    context.startActivity(Intent(context, MainActivity::class.java))
                                } catch (e: Exception) {
                                    // Display a nicer error message instead of default Firebase text
                                    error = friendlyAuthMessage(e, false)
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = email.isNotBlank()
                                && password.isNotBlank()
                                && confirmPassword.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0047AB),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Sign Up")
                    }

                    Spacer(Modifier.height(12.dp))

                    // Navigation link: Take the user back to Login
                    TextButton(
                        onClick = {
                            context.startActivity(Intent(context, MainActivity::class.java))
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Already have an account? Log in", color = Color(0xFF0047AB))
                    }
                }
            }

            // Loading spinner while Firebase processes signup
            if (isLoading) {
                Spacer(Modifier.height(16.dp))
                CircularProgressIndicator(color = Color(0xFF0047AB))
            }

            // Show the error returned from Firebase (like weak password)
            error?.let {
                Spacer(Modifier.height(16.dp))
                Text(it, color = Color.Red)
            }
        }
    }
}
