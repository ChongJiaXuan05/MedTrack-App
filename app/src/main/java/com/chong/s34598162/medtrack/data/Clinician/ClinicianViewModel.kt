package com.chong.s34598162.medtrack.data.Clinician

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chong.s34598162.medtrack.BuildConfig
import com.chong.s34598162.medtrack.data.MedCoachTips.MedCoachTipsRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Represents the UI state for loading aggregate statistics from Room.
sealed interface StatsUiState {
    object Loading : StatsUiState
    data class Success(val stats: ClinicianStatistics) : StatsUiState
    data class Error(val message: String) : StatsUiState
}

// Represent the UI state for the GenAI insights generation
sealed interface InsightUiState {
    object Initial : InsightUiState
    object Loading : InsightUiState
    data class Success(val text: String) : InsightUiState
    data class Error(val message: String) : InsightUiState
}

//ViewModel for the Clinician Dashboard screen.
//
// Responsibilities:
// Validates the clinician's access key before exposing any data.
// Fetches aggregate patient statistics from Room via [ClinicianRepository].
// Sends those statistics to the Gemini API and surfaces AI-generated insights.
class ClinicianViewModel(context: Context): ViewModel() {
    private val repository = ClinicianRepository(context)

    // Gemini generative model used to produce data driven clinician insights
    private val generativeModel = GenerativeModel(
        modelName = "gemini-3-flash-preview",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    // Whether the clinician has entered the correct access key
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    // Holds the current state of the aggregate stats load
    private val _statsUiState = MutableStateFlow<StatsUiState>(StatsUiState.Loading)
    val statsUiState: StateFlow<StatsUiState> = _statsUiState.asStateFlow()

    // Holds the current state of the GenAI insight generation
    private val _insightUiState = MutableStateFlow<InsightUiState>(InsightUiState.Initial)
    val insightUiState: StateFlow<InsightUiState> = _insightUiState.asStateFlow()

    // Validates the access key. Returns true on success so the UI can react.
    fun authenticate(enteredKey: String): Boolean {
        return if (enteredKey == "dollar-entry-apples") {
            _isAuthenticated.value = true
            loadStats()  // start loading stats immediately after login
            true
        } else {
            false
        }
    }

    // Loads all four aggregate statistics from Room
    private fun loadStats() {
        viewModelScope.launch(Dispatchers.IO) {
            _statsUiState.value = StatsUiState.Loading
            try {
                val stats = repository.getAggregateStats()
                _statsUiState.value = StatsUiState.Success(stats)
            } catch (e: Exception) {
                _statsUiState.value = StatsUiState.Error("Failed to load stats: ${e.message}")
            }
        }
    }

    // Sends aggregated patient data to Gemini and asks for 3 patterns/observations
    fun generateInsights() {
        // Only proceed if stats are available
        val stats = (_statsUiState.value as? StatsUiState.Success)?.stats ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _insightUiState.value = InsightUiState.Loading
            try {
                // Build a structured prompt embedding the four aggregate stats
                val prompt = """
                    You are a medical data analyst reviewing aggregate data from a patient medication tracking app.
                    Based on the statistics below, provide exactly 3 interesting patterns or observations.
                    Format your response as a numbered list: "1. ...", "2. ...", "3. ...".
                    Each insight should be 1-2 sentences, data-driven, and clinically relevant.
                    
                    Aggregate patient data:
                    - Total patients enrolled: ${stats.totalPatients}
                    - Average medications per patient: ${"%.1f".format(stats.avgMedicationPerPatient)}
                    - Most commonly reported symptom: ${stats.mostCommonSymptom}
                    - Average symptom severity: ${"%.1f".format(stats.avgSymptomSeverity)} / 10
                    
                    Provide 3 data-driven insights about this patient cohort.
                """.trimIndent()

                val response = generativeModel.generateContent(content { text(prompt) })
                val text = response.text ?: "No insights available."
                _insightUiState.value = InsightUiState.Success(text)
            } catch (e: Exception) {
                _insightUiState.value = InsightUiState.Error("Could not generate insights: ${e.message}")
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ClinicianViewModel(context.applicationContext) as T
    }

}