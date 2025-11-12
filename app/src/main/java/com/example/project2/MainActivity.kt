package com.example.project2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.project2.ui.theme.Project2Theme
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🎢 Universal Studios Guide", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    try {
                        AuthRepository.login(email, password)
                        Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                        context.startActivity(Intent(context, RideListActivity::class.java))
                    } catch (e: Exception) {
                        error = e.message
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    try {
                        AuthRepository.register(email, password)
                        Toast.makeText(context, "Account created!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        error = e.message
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Up")
        }

        if (isLoading) {
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator()
        }

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = Color.Red)
        }
    }
}

private fun friendlyAuthMessage(e: Exception, isLogin: Boolean): String {
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
            // occurs on sign-up when email already exists
            "An account with this email already exists. Try logging in."
        }
        is FirebaseAuthWeakPasswordException -> {
            "Password is too weak. Try at least 6-8 characters."
        }
        else -> {
            // network, unknown, etc.
            if (isLogin) "Couldn’t log in right now. Check your email/password or try again later."
            else "Couldn’t create your account. Please try again."
        }
    }
}