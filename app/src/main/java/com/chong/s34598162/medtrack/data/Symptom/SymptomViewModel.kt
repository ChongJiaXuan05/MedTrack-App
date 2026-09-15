package com.chong.s34598162.medtrack.data.Symptom

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing the current patient's symptom log.
 *
 * Responsibilities:
 * - Loading and refreshing symptoms from Room.
 * - Inserting new symptom entries logged by the patient.
 *
 * All database operations run on [Dispatchers.IO] to keep the main thread free.
 * The symptom list is exposed as a [StateFlow] so Compose can reactively observe changes.
 */
class SymptomViewModel(context: Context): ViewModel() {
    private val repository = SymptomRepository(context)

    // The full list of symptoms logged by the current patient
    private val _symptoms = MutableStateFlow<List<Symptom>>(emptyList())
    val symptoms: StateFlow<List<Symptom>> = _symptoms.asStateFlow()

    // ID of the currently logged-in patient
    private var currentPatientId: String = ""

    // Sets the current patient ID and loads their symptom history from Room.
    fun loadSymptoms(patientId: String) {
        currentPatientId = patientId
        refreshSymptoms()
    }

    //  Re-fetches the current patient's symptoms from Room and updates [symptoms].
    fun refreshSymptoms() {
        viewModelScope.launch(Dispatchers.IO) {
            _symptoms.value = repository.getSymptomsByPatientId(currentPatientId)
        }
    }

    // Inserts a new symptom log entry into Room and refreshes the symptom list.
    fun insertSymptom(symptom: Symptom) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertSymptom(symptom)
            refreshSymptoms()
        }
    }

    class SymptomViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SymptomViewModel(context.applicationContext) as T
    }
}