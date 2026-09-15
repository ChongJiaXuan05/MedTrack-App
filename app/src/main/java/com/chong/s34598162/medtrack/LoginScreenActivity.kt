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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.chong.s34598162.medtrack.data.Patient.ClaimResult
import com.chong.s34598162.medtrack.data.Patient.PatientViewModel
import com.chong.s34598162.medtrack.ui.theme.MedtrackTheme
import kotlinx.coroutines.launch


class LoginScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val patientViewModel: PatientViewModel = ViewModelProvider(
                this, PatientViewModel.PatientViewModelFactory(this@LoginScreenActivity)
            )[PatientViewModel::class.java]
            MedtrackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen(patientViewModel)
                }
            }
        }
    }
}

@Composable
fun LoginScreen(patientViewModel: PatientViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isClaimMode by remember { mutableStateOf(false) }

    var loginPatientId by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    var claimPatientId by remember { mutableStateOf("") }
    var claimPhone by remember { mutableStateOf("") }
    var claimNewPassword by remember { mutableStateOf("") }
    var claimConfirmPassword by remember { mutableStateOf("") }
    var claimError by remember { mutableStateOf("") }


    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = if (isClaimMode) "Claim Your Account"  else  "MedTrack Login",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            if (!isClaimMode) {
                // LOGIN FORM

                //PatientId Field
                OutlinedTextField(
                    value = loginPatientId,
                    onValueChange = {
                        loginPatientId = it; errorMessage = ""
                    },
                    label = { Text("Patient ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                //Password Field
                OutlinedTextField(
                    value = loginPassword,
                    onValueChange = {
                        loginPassword = it; errorMessage = ""
                    },
                    label = { Text("Password") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (errorMessage.isNotEmpty()) {
                    Text(errorMessage, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Login button
                Button(
                    onClick = {
                        // check phone and pass is not bank
                        if (loginPatientId.isBlank() || loginPassword.isBlank()) {
                            errorMessage = "Both field are required"
                            //Check phone number is valid or not
                        } else {
                            scope.launch {
                                if (!patientViewModel.isPatientIdValid(loginPatientId.trim())){
                                    errorMessage = "No account found with this ID"
                                } else if (!patientViewModel.loginByPatientId(loginPatientId.trim(), loginPassword)){
                                    errorMessage = "Incorrect password"
                                } else {
                                    val sharePref = context.getSharedPreferences("MedTrackPref", Context.MODE_PRIVATE)
                                    sharePref.edit().putString("logged_in_patient_id", loginPatientId.trim()).apply()
                                    context.startActivity(
                                        Intent(context, HomeActivity::class.java).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        }
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Login")
                }
            } else {
                // CLAIM ACCOUNT FORM

                OutlinedTextField(
                    value = claimPatientId,
                    onValueChange = {
                        claimPatientId = it; claimError = ""
                    },
                    label = { Text("Patient ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = claimPhone,
                    onValueChange = {
                        claimPhone = it; claimError = ""
                    },
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = claimNewPassword,
                    onValueChange = {
                        claimNewPassword = it; claimError = ""
                    },
                    label = { Text("New Password") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = claimConfirmPassword,
                    onValueChange = {
                        claimConfirmPassword = it; claimError = ""
                    },
                    label = { Text("Confirm Password") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (claimError.isNotEmpty()) {
                    Text(claimError, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        // check phone and pass is not bank
                        if (claimPatientId.isBlank() || claimPhone.isBlank() || claimNewPassword.isBlank() || claimConfirmPassword.isBlank()) {
                            errorMessage = "All fields are required"
                            return@Button
                        }
                        val passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$".toRegex()
                        if (!claimNewPassword.matches(passwordRegex)) {
                            claimError = "Password must be at least 8 characters with 1 letter and 1 number"
                            return@Button
                        }

                        if (claimNewPassword != claimConfirmPassword){
                            claimError = "Password do not match"
                            return@Button
                        }

                        scope.launch {
                            when (patientViewModel.claimAccount(
                                claimPatientId.trim(), claimPhone.trim(), claimNewPassword
                            )) {
                                ClaimResult.Success -> {
                                    snackbarHostState.showSnackbar("Account claimed! Please proceed to login")
                                    isClaimMode = false
                                }
                                ClaimResult.NotFound -> claimError = "Patient ID not found"
                                ClaimResult.PhoneMismatch -> claimError = "Phone number does not match our records"
                                ClaimResult.AlreadyClaimed -> claimError = "Account already claim"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Claim Account")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Toggle between Login and Claim
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isClaimMode) "Already claimed? " else "First time? ",
                    style = TextStyle(fontSize = 14.sp, color = Color.Gray)
                )
                Text(
                    text = if (isClaimMode) "Log In" else "Claim your account",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Blue,
                        textDecoration = TextDecoration.Underline

                    ),
                    modifier = Modifier.clickable {
                        isClaimMode = !isClaimMode
                        errorMessage = ""
                        claimError = ""
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account? ",
                    style = TextStyle(fontSize = 14.sp, color = Color.Gray)
                )
                Text(
                    text = "Sign Up ",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Blue,
                        textDecoration = TextDecoration.Underline

                    ),
                    modifier = Modifier.clickable {
                        context.startActivity(Intent(context, SignUpScreenActivity::class.java))
                    }
                )
            }
        }
    }
}
