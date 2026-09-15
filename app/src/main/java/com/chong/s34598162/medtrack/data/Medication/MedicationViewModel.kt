package com.chong.s34598162.medtrack.data.Medication

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chong.s34598162.medtrack.notifications.MedicationReminderManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewModel for managing the current patient's medication list.
 *
 * Responsibilities:
 * - Loading and refreshing medications from Room.
 * - Inserting new medications and scheduling their AlarmManager reminders.
 * - Deleting medications and cancelling their associated alarms.
 * - Updating the taken toggle and persisting the change to Room.
 * - Resetting all taken states at the start of each new day.
 *
 * All database operations run on [Dispatchers.IO] to keep the main thread free.
 */
class MedicationViewModel(context: Context): ViewModel() {
    private val repository = MedicationRepository(context)
    private val context = context.applicationContext

    // The full list of medications for the current patient
    private val _medications = MutableStateFlow<List<Medication>>(emptyList())
    val medications: StateFlow<List<Medication>> = _medications.asStateFlow()

    private var currentPatientId: String = ""

    // Sets the current patient ID, performs a daily reset if needed,
    // then loads the patient's medication list from Room.
    // Should be called once when HomeActivity is created.
    fun loadMedications(patientId: String) {
        currentPatientId = patientId
        viewModelScope.launch {
            resetDaily()
            _medications.value = repository.getMedicationsByPatientId(currentPatientId)
        }
    }

    // Re-fetches the current patient's medications from Room and updates [medications].
    fun refreshMedications() {
        viewModelScope.launch(Dispatchers.IO) {
            _medications.value = repository.getMedicationsByPatientId(currentPatientId)
        }
    }

    // Inserts a new medication into Room and schedules a daily AlarmManager reminder
    fun insertMedication(medication: Medication) {
        viewModelScope.launch(Dispatchers.IO) {
            val insertedId = repository.insertMedication(medication)
            if (insertedId > 0) {
                // Attach the Room-assigned ID before scheduling the alarm
                MedicationReminderManager.scheduleReminder(
                    context,
                    medication.copy(medicationId = insertedId.toInt())
                )
            }
            refreshMedications()
        }
    }

    // Updates the [Medication.isTaken] flag for a single medication.
    fun updateIsTaken(medicationId: Int, isTaken: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateIsTaken(medicationId, isTaken)
            refreshMedications()
        }
    }

    // Checks whether the calendar date has changed since the last app session.
    // If a new day is detected, resets all taken toggles for the current patient
    // and records today's date in SharedPreferences to prevent a repeated reset.
    private suspend fun resetDaily() {
        val prefs = context.getSharedPreferences("MedTrackPref", Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastDate = prefs.getString("last_taken_date", "") ?: ""

        if (today != lastDate) {
            // New day detected — reset all taken toggles for this patient
            repository.resetAllTaken(currentPatientId)
            // Record today so we don't reset again until tomorrow
            prefs.edit().putString("last_taken_date", today).apply()
        }
    }

    class MedicationViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MedicationViewModel(context.applicationContext) as T
    }

}