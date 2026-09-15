package com.chong.s34598162.medtrack

import android.graphics.drawable.Icon
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chong.s34598162.medtrack.data.Clinician.ClinicianStatistics
import com.chong.s34598162.medtrack.data.Clinician.ClinicianViewModel
import com.chong.s34598162.medtrack.data.Clinician.InsightUiState
import com.chong.s34598162.medtrack.data.Clinician.StatsUiState
import com.chong.s34598162.medtrack.data.Medication.MedicationViewModel
import com.chong.s34598162.medtrack.ui.theme.MedtrackTheme

class ClinicianActivity : ComponentActivity() {
    private lateinit var clinicianViewModel: ClinicianViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        clinicianViewModel = ViewModelProvider(
            this, ClinicianViewModel.Factory(this@ClinicianActivity)
        )[ClinicianViewModel::class.java]

        setContent {
            MedtrackTheme {
                val isAuthenticated by clinicianViewModel.isAuthenticated.collectAsState()

                if (!isAuthenticated) {
                    ClinicianLoginScreen(
                        onAuthenticated = { key -> clinicianViewModel.authenticate(key) },
                        onBack = { finish() }
                    )
                } else {
                    ClinicianDashboardScreen(
                        viewModel = clinicianViewModel,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}


// Login Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicianLoginScreen(
    onAuthenticated: (String) -> Boolean,
    onBack: () -> Unit
) {
    var accessKey by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clinician Access") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A40),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Clinician Login",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Enter your access key to view the clinician dashboard.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = accessKey,
                onValueChange = { accessKey = it; showError = false },
                label = { Text("Access Key") },
                // Hide the key as it's typed
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = showError,
                supportingText = {
                    if (showError) Text("Invalid access key. Please try again.", color = Color.Red)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val success = onAuthenticated(accessKey.trim())
                    if (!success) showError = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A40))
            ) {
                Text("ENTER DASHBOARD", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Dashboard Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicianDashboardScreen(
    viewModel: ClinicianViewModel,
    onBack: () -> Unit
) {
    val statsUiState by viewModel.statsUiState.collectAsState()
    val insightUiState by viewModel.insightUiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clinician Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A40),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Aggregate Statistics section ────────────────────────────────
            Text(
                "Aggregate Statistics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            when (val state = statsUiState) {
                is StatsUiState.Loading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is StatsUiState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Text(
                            state.message,
                            modifier = Modifier.padding(12.dp),
                            color = Color.Red
                        )
                    }
                }

                is StatsUiState.Success -> {
                    StatCards(state.stats)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // GenAI Insights section
            Text(
                "GenAI Insights",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { viewModel.generateInsights() },
                modifier = Modifier.fillMaxWidth(),
                enabled = statsUiState is StatsUiState.Success && insightUiState !is InsightUiState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A40))
            ) {
                if (insightUiState is InsightUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("FIND PATTERNS", color = Color.White, fontWeight = FontWeight.Bold)
            }

            when (val state = insightUiState) {
                is InsightUiState.Initial -> {
                    // Nothing shown before first tap
                }

                is InsightUiState.Loading -> {
                    // Spinner already shown inside button
                }

                is InsightUiState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Text(
                            state.message,
                            modifier = Modifier.padding(12.dp),
                            color = Color.Red
                        )
                    }
                }

                is InsightUiState.Success -> {
                    InsightCard(state.text)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

//  Stat Cards

@Composable
fun StatCards(stats: ClinicianStatistics) {
    // Four cards, each showing one aggregate statistic
    val items = listOf(
        Triple("Total Patients", "${stats.totalPatients}", Color(0xFFE3F2FD)),
        Triple("Avg Medications / Patient", "${"%.1f".format(stats.avgMedicationPerPatient)}", Color(0xFFE8F5E9)),
        Triple("Most Common Symptom", stats.mostCommonSymptom, Color(0xFFFFF8E1)),
        Triple("Avg Symptom Severity", "${"%.1f".format(stats.avgSymptomSeverity)} / 10", Color(0xFFFCE4EC))
    )

    items.forEach { (label, value, bgColor) ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = bgColor),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF1A1A40))
            }
        }
    }
}

// Insight Card

@Composable
fun InsightCard(text: String) {
    // Split on numbered list markers so each insight gets its own row
    val insights = text
        .split(Regex("(?=\\d+\\.)"))  // split before "1.", "2.", "3."
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Data Patterns",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF6A1B9A)
            )
            HorizontalDivider(color = Color(0xFFCE93D8))

            if (insights.size >= 2) {
                // Show each insight as its own block for readability
                insights.forEach { insight ->
                    Text(
                        insight,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                }
            } else {
                // Fallback: just show raw text if parsing didn't split cleanly
                Text(text, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
            }
        }
    }
}
