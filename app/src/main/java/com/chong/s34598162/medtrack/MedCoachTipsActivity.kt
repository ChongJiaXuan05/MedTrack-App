package com.chong.s34598162.medtrack

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chong.s34598162.medtrack.data.MedCoachTips.DrugUiState
import com.chong.s34598162.medtrack.data.MedCoachTips.MedCoachTipsViewModel
import com.chong.s34598162.medtrack.data.MedCoachTips.UiState
import com.chong.s34598162.medtrack.data.Medication.MedicationViewModel
import com.chong.s34598162.medtrack.data.Patient.PatientViewModel
import java.text.SimpleDateFormat
import androidx.compose.ui.platform.LocalLocale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedCoachScreen(
    patientId: String,
    patientViewModel: PatientViewModel,
    medicationViewModel: MedicationViewModel,
    medCoachTipsViewModel: MedCoachTipsViewModel
){
    var patientName by remember { mutableStateOf("") }

    LaunchedEffect(patientId) {
        patientName = patientViewModel.getPatientName(patientId)
        medCoachTipsViewModel.loadTips(patientId)
    }

    val medications by medicationViewModel.medications.collectAsState()
    val drugUiState by medCoachTipsViewModel.drugUiState.collectAsState()
    val tipUiState by medCoachTipsViewModel.tipUiState.collectAsState()
    val tips by medCoachTipsViewModel.tips.collectAsState()

    var searchText by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var showTipsDialog by remember { mutableStateOf(false) }

    val medicationNames = medications.map { it.name }.distinct()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("MedCoach", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        HorizontalDivider()

        // ═══════════════════════
        // TOP: DRUG INFORMATION
        // ═══════════════════════
        Text("Drug Information", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)

        if (medicationNames.isNotEmpty()) {
            // Pre-populated dropdown from patient's saved medications
            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = !dropdownExpanded }
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Select or type a medication name") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    medicationNames.forEach { name ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = { searchText = name; dropdownExpanded = false }
                        )
                    }
                }
            }
        } else {
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Enter medication name (e.g., ibuprofen)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        Button(
            onClick = { medCoachTipsViewModel.searchDrug(searchText.trim()) },
            modifier = Modifier.fillMaxWidth(),
            enabled = drugUiState !is DrugUiState.Loading  // disable while loading
        ) {
            if (drugUiState is DrugUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("SEARCH")
        }

        // Render drug search result based on current state
        when (val state = drugUiState) {
            is DrugUiState.Initial -> {} // nothing shown before first search

            is DrugUiState.Loading -> {} // spinner is already inside the button above

            is DrugUiState.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Text(state.errorMessage, modifier = Modifier.padding(12.dp), color = Color.Red)
                }
            }

            is DrugUiState.Success -> {
                val drug = state.drugInfo
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(drug.brandName, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        if (drug.genericName.isNotEmpty()) {
                            Text("Generic: ${drug.genericName}", fontSize = 13.sp, color = Color.Gray)
                        }
                        HorizontalDivider()
                        DrugInfoSection("Purpose", drug.purpose)
                        DrugInfoSection("Warnings", drug.warnings)
                        DrugInfoSection("Dosage & Administration", drug.dosage)
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // ═══════════════════════════
        // BOTTOM: GENAI TIPS
        // ═══════════════════════════
        Text("GenAI Medication Tips", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)

        Button(
            onClick = { medCoachTipsViewModel.generateTip(patientName) },
            modifier = Modifier.fillMaxWidth(),
            enabled = tipUiState !is UiState.Loading,  // disable while waiting for Gemini
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A40))
        ) {
            if (tipUiState is UiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("GENERATE TIP", color = Color.White)
        }

        // Render tip result based on current state
        when (val state = tipUiState) {
            is UiState.Initial -> {} // nothing shown before first generate

            is UiState.Loading -> {} // spinner is already inside the button above

            is UiState.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Text(state.errorMessage, modifier = Modifier.padding(12.dp), color = Color.Red)
                }
            }

            is UiState.Success -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Your Tip", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(state.outputText, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        OutlinedButton(
            onClick = { showTipsDialog = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = tips.isNotEmpty()
        ) {
            Text("SHOW ALL TIPS (${tips.size})")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Tip history dialog
    if (showTipsDialog) {
        AlertDialog(
            onDismissRequest = { showTipsDialog = false },
            title = { Text("Tip History") },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(tips) { tip ->
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(
                                SimpleDateFormat("dd/MM/yyyy HH:mm", LocalLocale.current.platformLocale)
                                    .format(java.util.Date(tip.timestamp)),
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(tip.tip, style = MaterialTheme.typography.bodyMedium)
                            HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTipsDialog = false }) { Text("Close") }
            }
        )
    }
}

@Composable
fun DrugInfoSection(title: String, content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(content, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
    }
}