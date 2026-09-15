package com.chong.s34598162.medtrack.data.MedCoachTips

import com.chong.s34598162.medtrack.network.DrugInfo

interface UiState{
    object Initial : UiState
    object Loading : UiState
    data class Success(val outputText: String) : UiState
    data class Error(val errorMessage: String) : UiState
}
interface DrugUiState {
    object Initial : DrugUiState                              // no search done yet
    object Loading : DrugUiState                              // waiting for OpenFDA response
    data class Success(val drugInfo: DrugInfo) : DrugUiState  // drug found
    data class Error(val errorMessage: String) : DrugUiState  // not found or network error
}


