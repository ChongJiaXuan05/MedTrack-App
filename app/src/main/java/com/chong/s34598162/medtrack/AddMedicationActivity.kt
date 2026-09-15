package com.chong.s34598162.medtrack

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Context
import android.icu.util.Calendar
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.chong.s34598162.medtrack.data.Medication.Medication
import com.chong.s34598162.medtrack.data.Medication.MedicationViewModel
import com.chong.s34598162.medtrack.ui.theme.MedtrackTheme
import kotlinx.coroutines.launch
import java.util.Locale

class AddMedicationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val medicationViewModel = ViewModelProvider(
            this, MedicationViewModel.MedicationViewModelFactory(this@AddMedicationActivity)
        )[MedicationViewModel::class.java]

        setContent {
            MedtrackTheme {
                AddMedicationScreen(medicationViewModel)
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(medicationViewModel: MedicationViewModel) {
    val context = LocalContext.current
    val sharePref = context.getSharedPreferences("MedTrackPref", Context.MODE_PRIVATE)
    val loggedInPatientID = sharePref.getString("logged_in_patient_id", "")

    var medName by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    val dosageRegex = "^\\d+(\\.\\d+)?(mg|ml|g)\$".toRegex()

    val scheduledTime = remember { mutableStateOf("") }
    val timePickerDialog = medicationTimePickerDialog(scheduledTime)

    var notes by remember { mutableStateOf("") }

    //Frequency dropdown state
    val frequencyOption = listOf("Once daily", "Twice daily", "Three times daily", "As needed")
    var frequencyExpanded by remember { mutableStateOf(false) }
    var selectedFrequency by remember { mutableStateOf(frequencyOption[0])}

    // Medication type drop down state
    val typeOptions = listOf("Tablet", "Capsule", "Liquid", "Injection", "Topical", "Other")
    var typeExpanded by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(typeOptions[0]) }


    val snackbarHostState = remember { SnackbarHostState() }
    // Allow to run suspend function (snackbar function)
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ADD NEW MEDICATION", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            //Medication Name
            OutlinedTextField(
                value = medName,
                onValueChange = {medName = it},
                label = { Text("Medication Name *")},
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            //Dosage
            OutlinedTextField(
                value = dosage,
                onValueChange = {dosage = it},
                label = { Text("Dosage *")},
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = dosage.isNotEmpty() && !dosage.matches(dosageRegex),
                supportingText = {
                    if (dosage.isNotEmpty() && !dosage.matches(dosageRegex)) {
                        Text("Format: number + unit (e.g., 500mg, 10ml)", color = Color.Red)
                    }
                }
            )

            //Frequency dropdown
            ExposedDropdownMenuBox(
                expanded = frequencyExpanded,
                onExpandedChange = { frequencyExpanded = !frequencyExpanded}
            ) {
                OutlinedTextField(
                    value = selectedFrequency,
                    onValueChange = {},
                    readOnly = true,
                    label = {Text("Frequency*")},
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = frequencyExpanded) },
                    modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = frequencyExpanded,
                    onDismissRequest = {frequencyExpanded = false}
                ) {
                    frequencyOption.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option)},
                            onClick = {
                                selectedFrequency = option
                                frequencyExpanded = false
                            }
                        )
                    }
                }
            }

            //Scheduled Time
            OutlinedTextField(
                value = scheduledTime.value,
                onValueChange = {},
                label = { Text("Scheduled Time *") },
                modifier = Modifier.fillMaxWidth().clickable{timePickerDialog.show()},
                readOnly = true,
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            //Medication type dropdown
            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                onExpandedChange = { typeExpanded = !typeExpanded}
            ) {
                OutlinedTextField(
                    value = selectedType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Medication Type*") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                    modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false }
                ) {
                    typeOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedType = option
                                typeExpanded = false
                            }
                        )
                    }
                }
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save and Clear Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                //Save Button
                Button(
                    onClick = {
                        if (medName.isBlank() || dosage.isBlank() || scheduledTime.value.isBlank()){
                            scope.launch {
                                snackbarHostState.showSnackbar("Please fill all required fields!")
                            }
                        } else if (!dosage.matches(dosageRegex)){
                            scope.launch {
                                snackbarHostState.showSnackbar("Invalid dosage")
                            }
                        } else {
                            medicationViewModel.insertMedication(
                                Medication(
                                    patientId = loggedInPatientID,
                                    name = medName,
                                    dosage = dosage,
                                    frequency = selectedFrequency,
                                    scheduledTime = scheduledTime.value,
                                    type = selectedType,
                                    notes = notes
                                )
                            )
                            scope.launch {
                                snackbarHostState.showSnackbar("Mediation added successfully!")
                                kotlinx.coroutines.delay(500)
                                (context as? android.app.Activity)?.finish()
                            }
                            medName = ""; dosage = ""; scheduledTime.value =""; notes = ""

                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A40))
                ) {
                    Text("SAVE", color = Color.White)
                }

                //Clear Button
                OutlinedButton(
                    onClick = {
                        medName = ""; dosage = ""; scheduledTime.value = ""; notes = ""
                        selectedFrequency = frequencyOption[0]
                        selectedType = typeOptions[0]
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }


            }
        }
    }
}

@Composable
fun medicationTimePickerDialog(mTime:MutableState<String>): TimePickerDialog {
    val mContext = LocalContext.current

    val mCalendar = Calendar.getInstance()

    val mHour = mCalendar.get(Calendar.HOUR_OF_DAY)
    val mMinute = mCalendar.get(Calendar.MINUTE)

    mCalendar.time = Calendar.getInstance().time

    return TimePickerDialog(
        mContext,
        {_, mHour, mMinutes ->
            mTime.value = String.format(Locale.getDefault(), "%02d:%02d", mHour, mMinutes)
        }, mHour, mMinute, false
    )
}