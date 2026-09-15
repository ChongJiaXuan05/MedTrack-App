package com.chong.s34598162.medtrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.chong.s34598162.medtrack.data.Patient.PatientViewModel
import com.chong.s34598162.medtrack.ui.theme.MedtrackTheme
import kotlinx.coroutines.launch

class SignUpScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val patientViewModel = ViewModelProvider(
            this, PatientViewModel.PatientViewModelFactory(this)
        )[PatientViewModel::class.java]

        setContent {
            MedtrackTheme {
                SignUpScreen(patientViewModel = patientViewModel,
                    onNavigationToLogin = {
                    finish() // Close the sign-up screen and return to log in screen
                })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    patientViewModel: PatientViewModel,
    onNavigationToLogin: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    //Sign up form states
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmedPassword by remember { mutableStateOf("") }

    //Error states
    var nameError by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmedPasswordError by remember { mutableStateOf("") }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(title = {
                Text(
                    "CREATE ACCOUNT",
                    fontWeight = FontWeight.Bold
                )
            })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Full name text field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = "" },
                label = { Text("Full Name *") },
                modifier = Modifier.fillMaxWidth(),
                isError = nameError.isNotEmpty(),
                supportingText = { if (nameError.isNotEmpty()) Text(nameError, color = Color.Red) }
            )

            // Phone number text field
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it; phoneError = "" },
                label = { Text("Phone Number *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = phoneError.isNotEmpty(),
                supportingText = {
                    if (phoneError.isNotEmpty()) Text(
                        phoneError,
                        color = Color.Red
                    )
                }
            )

            // Password text field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; passwordError = "" },
                label = { Text("Password *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordError.isNotEmpty(),
                supportingText = {
                    if (passwordError.isNotEmpty()) Text(
                        passwordError,
                        color = Color.Red
                    )
                }
            )

            // Confirmed Password text field
            OutlinedTextField(
                value = confirmedPassword,
                onValueChange = { confirmedPassword = it; confirmedPasswordError = "" },
                label = { Text("Confirmed Password *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                isError = confirmedPasswordError.isNotEmpty(),
                supportingText = {
                    if (confirmedPasswordError.isNotEmpty()) Text(
                        confirmedPasswordError,
                        color = Color.Red
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // Reset error field
                    nameError = ""; phoneError = ""; passwordError = ""; confirmedPasswordError = ""

                    //Validation Logic
                    if (name.isBlank()) {
                        nameError = "Name is required"
                    }

                    if (!phone.startsWith("04") || phone.length != 10) {
                        phoneError = "phone number should starts with 04 and exactly 10 digits"
                    }

                    val passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$".toRegex()
                    if (!password.matches(passwordRegex)) {
                        passwordError =
                            "Password should be at least 8 characters, 1 letter and 1 number"
                    }

                    if (confirmedPassword != password) {
                        confirmedPasswordError = "Passwords do not match"
                    }

                    //Success Path
                    if (nameError.isEmpty() && phoneError.isEmpty() && passwordError.isEmpty() && confirmedPasswordError.isEmpty()) {
                        scope.launch {
                            val phoneTaken = patientViewModel.phoneExist(phone.trim())
                            if (phoneTaken) {
                                phoneError = "Phone number already registered"
                                return@launch
                            }

                            //Save new patient to Room DB
                            val newPatientId = patientViewModel.registerNewUser(
                                name.trim(), phone.trim(), password
                            )

                            // Store session so the user is logged in immediately after sign-up
                            val sharePref =
                                context.getSharedPreferences("MedTrackPref", Context.MODE_PRIVATE)
                            sharePref.edit().putString("logged_in_patient_id", newPatientId).apply()

                            snackbarHostState.showSnackbar("Account created successfully!")
                            kotlinx.coroutines.delay(500)

                            // Go straight to Home
                            context.startActivity(
                                Intent(context, HomeActivity::class.java).apply {
                                    flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("SIGN UP")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    style = TextStyle(fontSize = 14.sp, color = Color.Gray)
                )
                Text(
                    text = "Login ",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Blue,
                        textDecoration = TextDecoration.Underline

                    ),
                    modifier = Modifier.clickable {
                        context.startActivity(Intent(context, LoginScreenActivity::class.java))
                    }
                )
            }
        }
    }
}





