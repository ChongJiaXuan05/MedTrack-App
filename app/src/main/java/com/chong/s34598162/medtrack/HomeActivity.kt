package com.chong.s34598162.medtrack

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.DatePicker
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chong.s34598162.medtrack.data.MedCoachTips.MedCoachTipsViewModel
import com.chong.s34598162.medtrack.data.Medication.Medication
import com.chong.s34598162.medtrack.data.Medication.MedicationViewModel
import com.chong.s34598162.medtrack.data.Patient.PatientViewModel
import com.chong.s34598162.medtrack.data.Symptom.Symptom
import com.chong.s34598162.medtrack.data.Symptom.SymptomViewModel
import com.chong.s34598162.medtrack.notifications.MedicationReminderManager
import com.chong.s34598162.medtrack.ui.theme.MedtrackTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeActivity : ComponentActivity() {
    private lateinit var medicationViewModel: MedicationViewModel
    private lateinit var symptomViewModel: SymptomViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Create notification channel
        MedicationReminderManager.createNotificationChannel(this)

        // Request POST_NOTIFICATIONS permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
        }

        medicationViewModel = ViewModelProvider(
            this, MedicationViewModel.MedicationViewModelFactory(this@HomeActivity)
        )[MedicationViewModel::class.java]

        val patientViewModel = ViewModelProvider(
            this, PatientViewModel.PatientViewModelFactory(this@HomeActivity)
        )[PatientViewModel::class.java]

        symptomViewModel = ViewModelProvider(
            this, SymptomViewModel.SymptomViewModelFactory(this@HomeActivity)
        )[SymptomViewModel::class.java]

        val medCoachTipsViewModel = ViewModelProvider(
            this, MedCoachTipsViewModel.MedCoachTipsViewModelFactory(this@HomeActivity)
        )[MedCoachTipsViewModel::class.java]

        val sharePref = getSharedPreferences("MedTrackPref", MODE_PRIVATE)
        val patientID = sharePref.getString("logged_in_patient_id", "") ?: ""

        medicationViewModel.loadMedications(patientID)
        symptomViewModel.loadSymptoms(patientID)

        setContent {
            MedtrackTheme {
                val navController = rememberNavController()
                val context = LocalContext.current

                val sharePref = context.getSharedPreferences("MedTrackPref", MODE_PRIVATE)
                val patientID = sharePref.getString("logged_in_patient_id", "") ?: ""

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        BottomNavigationBar(navController)
                    }
                ) { innerPadding ->
                    // call NavHost to display content
                    MyNavHost(innerPadding, navController, patientID,
                        medicationViewModel,patientViewModel,symptomViewModel, medCoachTipsViewModel)

                }
            }


        }
    }

    override fun onResume() {
        super.onResume()
        medicationViewModel.refreshMedications()
        symptomViewModel.refreshSymptoms()
    }
}

@Composable
fun HomeScreen(
    patientID: String,
    medicationViewModel: MedicationViewModel,
    patientViewModel: PatientViewModel
) {
    val context = LocalContext.current
    var patientName by remember { mutableStateOf("Patient") }
    LaunchedEffect(patientID) {
        patientName = patientViewModel.getPatientName(patientID)
    }

    val medications by medicationViewModel.medications.collectAsState()

    val todayDate = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date())
    val takenCount = medications.count {it.isTaken}

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row() {
            Column {
                Text("Hello, $patientName", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(todayDate, color = Color.Gray, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(130.dp))

            //Logout Button
            IconButton(
                onClick = {
                    // Clear session id
                    val sharePref = context.getSharedPreferences("MedTrackPref", Context.MODE_PRIVATE)
                    sharePref.edit().putString("logged_in_patient_id", "").apply()

                    val intent = Intent(context, MainActivity::class.java)
                    // Guard against back-stack issues by clear the activity history
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                },
                // This forces the text and icon inside to be Red
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.Black)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Logout",
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Summary card showing number of taken medicine
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFE3F2FD),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = "$takenCount of ${medications.size} medications taken today",
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A40)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display empty state
        if (medications.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No medications scheduled", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(medications, key = { it.medicationId }) { medication ->
                    MedicationCard(medication) { isChecked ->
                        medicationViewModel.updateIsTaken(medication.medicationId, isChecked)
                    }
                }
            }
        }

        // Add Medications button
        Button(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A40)),
            onClick = {
                context.startActivity(Intent(context, AddMedicationActivity::class.java))
            }

        ) {
            Text("+ ADD MEDICATION", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun MedicationCard(medication: Medication, onToggle: (Boolean) -> Unit) {
    val textDecoration = if (medication.isTaken) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(medication.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, textDecoration = textDecoration)
                Text("${medication.dosage} - ${medication.frequency}", fontSize = 14.sp)
                Text("Type: ${medication.type}", fontSize = 14.sp, color = Color.Gray)
                Text("Scheduled: ${medication.scheduledTime}", color = Color.Gray, fontSize = 12.sp)
                if (medication.notes.isNotEmpty()) {
                    Text(medication.notes, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
            Checkbox(
                checked = medication.isTaken,
                onCheckedChange = { onToggle(it) }
            )
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController){
    var selectedItem by remember { mutableIntStateOf(0) }

    val items = listOf("home", "symptoms", "medcoach", "settings")

    NavigationBar(
        containerColor = Color(0xFF1A1A40)
    )  {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    when (item) {
                        "home" -> Icon(Icons.Filled.Home, contentDescription = "Home")
                        "symptoms" -> Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Symptoms")
                        "medcoach" -> Icon(Icons.Filled.Info, contentDescription = "MedCoach")
                        "settings" -> Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                },
                label = { Text(item.uppercase(), color = Color.White)},
                selected = selectedItem == index,
                onClick = {
                    selectedItem = index
                    navController.navigate(item){
                        // clear backstack to avoid memory leaks
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Composable
fun MyNavHost(
    innerPadding: PaddingValues,
    navController: NavHostController,
    patientID: String,
    medicationViewModel: MedicationViewModel,
    patientViewModel: PatientViewModel,
    symptomViewModel: SymptomViewModel,
    medCoachTipsViewModel: MedCoachTipsViewModel) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.padding(innerPadding).fillMaxSize()
    ){
        composable("home"){
            HomeScreen(patientID, medicationViewModel, patientViewModel)
        }
        composable("symptoms"){
            SymptomsScreen(patientID, symptomViewModel)
        }
        composable("settings") {
            SettingScreen(patientID,patientViewModel)
        }
        composable("medcoach") {
            MedCoachScreen(patientID, patientViewModel, medicationViewModel,medCoachTipsViewModel)
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomsScreen(patientID: String, symptomViewModel: SymptomViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Form state
    val categories = listOf("Pain", "Nausea", "Dizziness", "Fatigue", "Headache", "Skin Reaction", "Other")
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var categoryExpanded by remember { mutableStateOf(false) }

    var severity by remember { mutableFloatStateOf(5f) }
    var symptomNotes by remember { mutableStateOf("") }
    var symptomDateTime by remember { mutableStateOf("") }

    //InlineError state
    var showError by remember { mutableStateOf(false) }

    // Create a specific state for date and time
    val dateState = remember { mutableStateOf("") }
    val timeState = remember { mutableStateOf("") }

    // Initialize your custom pickers
    val mDatePickerDialog = DatePickerFun(dateState)
    val mTimePickerDialog = TimePickerFun(timeState)

    // Update your combined symptomDateTime whenever dateState or timeState changes
    LaunchedEffect(dateState.value, timeState.value) {
        if (dateState.value.isNotEmpty() || timeState.value.isNotEmpty()) {
            symptomDateTime = "${dateState.value} ${timeState.value}".trim()
        }
    }

    val notesMaxChar = 200

    // Calculate dynamic feedback values (Slider)
    val currentSeverityInt = severity.toInt()
    val (severityLabel, severityColor) = when (currentSeverityInt) {
        in 1..3 -> "Mild" to Color(0xFF4CAF50)      // Green
        in 4..6 -> "Moderate" to Color(0xFFFF9800)  // Amber/Orange
        else -> "Severe" to Color(0xFFF44336)       // Red
    }

    val symptoms by symptomViewModel.symptoms.collectAsState(initial = emptyList())

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState)}
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                "Log Symptom",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            //Category Dropdown Menu
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category *") },
                    isError = showError && selectedCategory.isEmpty(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.menuAnchor(
                        type = MenuAnchorType.PrimaryNotEditable,
                        enabled = true
                    )
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categories.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedCategory = option
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }
            if (showError && selectedCategory.isEmpty()) {
                Text(
                    "Please select a category",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Severity Slider
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Severity: $currentSeverityInt",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                //Dynamic Label with Color Background
                Surface(
                    color = severityColor,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = severityLabel.uppercase(),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Slider(
                value = severity,
                onValueChange = { severity = it },
                valueRange = 1f..10f,
                steps = 8,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = severityColor,
                    activeTrackColor = severityColor,
                    inactiveTrackColor = severityColor.copy(0.24f)
                )
            )

            // Date Time Picker
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { mDatePickerDialog.show() }, // CALL .show() HERE
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (dateState.value.isEmpty()) "Select Date" else dateState.value)
                }

                Button(
                    onClick = { mTimePickerDialog.show() }, // CALL .show() HERE
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (timeState.value.isEmpty()) "Select Time" else timeState.value)
                }
            }

            if (showError && (dateState.value.isEmpty() || timeState.value.isEmpty())) {
                Text("Date and Time are required",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }

            //Notes field
            OutlinedTextField(
                value = symptomNotes,
                onValueChange = { if (it.length <= notesMaxChar) symptomNotes = it },
                label = {Text("Notes (Optional)")},
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    Text(text = "${symptomNotes.length} / $notesMaxChar",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End)
                }
            )

            //Save Button
            Button(
                onClick = {
                    if (dateState.value.isEmpty()|| timeState.value.isEmpty() || selectedCategory.isEmpty()) {
                        showError = true
                        scope.launch { snackbarHostState.showSnackbar("Please enter all required fields)")}
                    } else {
                        showError = false
                        symptomViewModel.insertSymptom(
                            Symptom(
                                patientId = patientID,
                                category = selectedCategory,
                                severity = severity.toInt(),
                                dateTime = symptomDateTime,
                                notes = symptomNotes
                            )
                        )
                        scope.launch { snackbarHostState.showSnackbar("Symptom logged") }
                        //Reset form
                        symptomNotes = ""; symptomDateTime = ""; severity = 5f
                        dateState.value = ""; timeState.value = ""
                    }
                },
                modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
            ) {
                Text("SAVE")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 2.dp)

            // History
            Text("Symptom History", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            if (symptoms.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No symptoms logged yet.", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(symptoms, key = {it.symptomId}) { symptom ->
                        SymptomCard(symptom)
                    }
                }
            }
        }
    }
}

@Composable
fun SymptomCard(symptom: Symptom) {
    val (label, color) = when (symptom.severity) {
        in 1..3 -> "Mild" to Color(0xFF4CAF50)
        in 4..6 -> "Moderate" to Color(0xFFFF9800)
        else -> "Severe" to Color(0xFFF44336)
    }
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(symptom.category, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Surface(color = color, shape = MaterialTheme.shapes.small) {
                    Text(label, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelMedium)
                }
            }
            Text(symptom.dateTime, fontSize = 12.sp, color = Color.Gray)
            if (symptom.notes.isNotEmpty()) {
                Text(symptom.notes, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun SettingScreen(patientId: String, patientViewModel: PatientViewModel) {
    val context = LocalContext.current
    var patientName by remember { mutableStateOf("") }
    var patientPhone by remember { mutableStateOf("") }

    LaunchedEffect(patientId) {
        patientName = patientViewModel.getPatientName(patientId)
        patientPhone = patientViewModel.getPatientPhone(patientId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Patient Information", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                HorizontalDivider()
                Text("ID: $patientId", fontSize = 16.sp)
                Text("Name: $patientName", fontSize = 16.sp)
                Text("Phone: $patientPhone", fontSize = 16.sp)
            }
        }

        // Clinician Login — navigates to ClinicianActivity where access key is validated
        Button(
            onClick = {
                context.startActivity(Intent(context, ClinicianActivity::class.java))
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A40))
        ) {
            Text("CLINICIAN LOGIN", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                var sharedPref = context.getSharedPreferences("MedTrackPref", Context.MODE_PRIVATE)
                sharedPref.edit().putString("logged_in_patient_id", "").apply()
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("LOGOUT", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DatePickerFun(mDate: MutableState<String>): DatePickerDialog {
    val mContext = LocalContext.current
    val mCalendar = Calendar.getInstance()

    val mYear = mCalendar.get(Calendar.YEAR)
    val mMonth = mCalendar.get(Calendar.MONTH)
    val mDay = mCalendar.get(Calendar.DAY_OF_MONTH)

    return DatePickerDialog(
        mContext,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            // Update the state directly
            mDate.value = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year)
        }, mYear, mMonth, mDay
    )
}

@Composable
fun TimePickerFun(mTime: MutableState<String>): TimePickerDialog {
    val mContext = LocalContext.current
    val mCalendar = Calendar.getInstance()

    val mHour = mCalendar.get(Calendar.HOUR_OF_DAY)
    val mMinute = mCalendar.get(Calendar.MINUTE)

    return TimePickerDialog(
        mContext,
        { _, hour, minute ->
            // Update the state directly, formatting with leading zeros
            mTime.value = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
        }, mHour, mMinute, false
    )
}
