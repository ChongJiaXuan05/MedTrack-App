package com.chong.s34598162.medtrack.data.MedCoachTips

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.chong.s34598162.medtrack.BuildConfig

/**
 * ViewModel for the MedCoach screen.
 *
 * Responsibilities:
 * - Drug information lookup via the OpenFDA API ([searchDrug]).
 * - Personalised AI medication tip generation via the Gemini API ([generateTip]).
 * - Tip history management — saving generated tips to Room and exposing them as a [StateFlow].
 *
 * UI states are exposed as [StateFlow] so Compose can reactively observe changes.
 */
class MedCoachTipsViewModel(context: Context): ViewModel() {

    private val repository = MedCoachTipsRepository(context)

    // Gemini generative model used to produce personalised medication tips
    private val generativeModel = GenerativeModel(
        modelName = "gemini-3-flash-preview",
        apiKey = BuildConfig.GEMINI_API_KEY,
    )

    // ID of the currently logged-in patient
    private  var currentPatientId = ""

    // Previously generated tips for the current patient
    private val _tips = MutableStateFlow<List<MedCoachTips>>(emptyList())
    val tips: StateFlow<List<MedCoachTips>> = _tips.asStateFlow()

    // Tips generation state
    private val _tipUiState = MutableStateFlow<UiState>(UiState.Initial)
    val tipUiState: StateFlow<UiState> = _tipUiState.asStateFlow()

    // Drug search state
    private val _drugUiState = MutableStateFlow<DrugUiState>(DrugUiState.Initial)
    val drugUiState: StateFlow<DrugUiState> = _drugUiState.asStateFlow()

    // Sets the current patient ID and loads their saved tip history from Room
    fun loadTips(patientId: String) {
        currentPatientId = patientId
        refreshTips()
    }

    // Reloads the tip history for the current patient from Room and update tips
    private fun refreshTips() {
        viewModelScope.launch {
            _tips.value = repository.getTipsByPatientId(currentPatientId)
        }
    }

    // Drug Search
    fun searchDrug(drugName: String) {
        if (drugName.isBlank()) return
        viewModelScope.launch {
            _drugUiState.value = DrugUiState.Loading
            try {
                // Check network ability
                if (!repository.isNetworkAvailable()) {
                    _drugUiState.value = DrugUiState.Error("No network connection. Please check your internet.")
                } else {
                    // queries
                    val result = repository.searchDrug(drugName)
                    if (result != null) {
                        _drugUiState.value = DrugUiState.Success(result)
                    } else {
                        _drugUiState.value = DrugUiState.Error("\"$drugName\" not found. Try the generic name (e.g., ibuprofen).")
                    }
                }
            } catch (e: Exception) {
                _drugUiState.value = DrugUiState.Error("Search failed: ${e.message}")
            }
        }
    }

    //  Generates a personalized medication tip for the current patient using Gemini.
    fun generateTip(patientName: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            _tipUiState.value = UiState.Loading
            try {
                if (!repository.isNetworkAvailable()) {
                    _tipUiState.value = UiState.Error("No network connection. Please check your internet.")
                } else {
                    // Fetch patient data from database
                    //Get the full medications list for patient
                    val medications = repository.getPatientMedications(currentPatientId)

                    // Get the most recent 5 symtoms fot this patient
                    val symptoms = repository.getPatientSymptoms(currentPatientId).take(5)

                    // Format medication and symptoms list to readable format
                    val medicationContext = if (medications.isNotEmpty()) {
                        medications.joinToString("\n") { med ->
                            "- ${med.name}, ${med.dosage}, ${med.frequency} (${med.type})"
                        }
                    } else {
                        "No medications recorded."
                    }

                    val symptomContext = if (symptoms.isNotEmpty()) {
                        symptoms.joinToString("\n") { sym ->
                            "- ${sym.category}, severity ${sym.severity}/10 on ${sym.dateTime}"
                        }
                    } else {
                        "No symptoms recorded."
                    }

                    //Build personalise promp
                    val nameLabel = if (patientName.isNotEmpty()) patientName else "The patient"
                    val prompt = """
                        You are a helpful and friendly medical coach assistant.
                        Based on the patient information below, generate a short (2-3 sentences), 
                        personalised, and encouraging medication tip.
                        - Address the patient by name.
                        - Reference their specific medications or recent symptoms where relevant.
                        - Keep the tone warm, supportive, and practical.
                        
                        Patient name: $nameLabel
                        
                        Current medications:
                        $medicationContext
                        
                        Recent symptoms (last 5 recorded):
                        $symptomContext
                        
                        Generate a personalised tip that is directly relevant to this patient's situation.
                    """.trimIndent()

                    val response = generativeModel.generateContent(
                        content { text(prompt) }
                    )
                    val tip = response.text
                        ?: "Stay consistent with your medications — your health is worth it!"

                    // Save tip to DB and refresh history
                    _tipUiState.value = UiState.Success(tip)
                    repository.insertTip(MedCoachTips(patientId = currentPatientId, tip = tip))
                    refreshTips()
                }
            } catch (e: Exception) {
                _tipUiState.value = UiState.Error("Gemini error: ${e.message}.")
            }
        }
    }


    class MedCoachTipsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MedCoachTipsViewModel(context.applicationContext) as T
    }
}