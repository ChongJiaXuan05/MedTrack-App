package com.chong.s34598162.medtrack.data.MedCoachTips

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.chong.s34598162.medtrack.data.MedTrackDatabase
import com.chong.s34598162.medtrack.data.Medication.Medication
import com.chong.s34598162.medtrack.data.Medication.MedicationRepository
import com.chong.s34598162.medtrack.data.Symptom.Symptom
import com.chong.s34598162.medtrack.data.Symptom.SymptomRepository
import com.chong.s34598162.medtrack.network.DrugInfo
import com.chong.s34598162.medtrack.network.DrugLabelResult
import com.chong.s34598162.medtrack.network.OpenFdaApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Repository for the MedCoach feature, handling two main responsibilities:
 * 1. Drug information lookup via the OpenFDA REST API.
 * 2. AI tip storage and retrieval via Room, and patient context fetching
 *    (medications and symptoms) for personalized Gemini prompts.
 *
 * All network operations run on [Dispatchers.IO].
 */
class MedCoachTipsRepository(private val applicationContext: Context) {
    private val medCoachTipsDao = MedTrackDatabase.getDatabase(applicationContext).medCoachTipsDao()

    // reuse the existing repositories to access medications and symptoms.
    private val medicationRepository = MedicationRepository(applicationContext)
    private val symptomRepository = SymptomRepository(applicationContext)

    // Retrofit client pointed at the OpenFDA drug label endpoint
    private val openFdaApiService = Retrofit.Builder()
        .baseUrl("https://api.fda.gov/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OpenFdaApiService::class.java)

    // Database

    // Insert an AI-generated tip to the local database.
    suspend fun insertTip(tip: MedCoachTips) {
        medCoachTipsDao.insertTip(tip)
    }


    // Retrieves all saved tips for the given patient
    suspend fun getTipsByPatientId(patienId: String): List<MedCoachTips> {
        return medCoachTipsDao.getTipsByPatientId(patienId)
    }

    // Fetches all medications belonging to the given patient
    suspend fun getPatientMedications(patienId: String): List<Medication> {
        return  medicationRepository.getMedicationsByPatientId(patienId)
    }


    //Fetches all symptoms logged by the given patient.
    suspend fun getPatientSymptoms(patienId: String): List<Symptom> {
        return  symptomRepository.getSymptomsByPatientId(patienId)
    }

    // OpenFda

    // Searches the OpenFDA drug label database for the given drug name.
    // First attempts a brand-name search, if no results are found, falls back
    // to a generic-name search. Returns null if neither search yields a result.
    suspend fun searchDrug(drugName: String): DrugInfo? {
        return withContext(Dispatchers.IO) {
            // // Try brand name first, then fall back to generic name
            var response = openFdaApiService.searchDrug("""openfda.brand_name:"$drugName"""")
            if (response.results.isNullOrEmpty()) {
                response = openFdaApiService.searchDrug("""openfda.generic_name:"$drugName"""")
            }
            val result = response.results?.firstOrNull() ?: return@withContext null
            DrugInfo(
                brandName = result.openfda?.brandName?.firstOrNull() ?: drugName,
                genericName = result.openfda?.genericName?.firstOrNull() ?: "",
                purpose =  result.purpose?.firstOrNull() ?: "Not available",
                warnings = result.warnings?.firstOrNull() ?: "Not available",
                dosage = result.dosageAndAdministration?.firstOrNull() ?: "Not available"
            )
        }
    }

    // checking Network
    fun isNetworkAvailable(): Boolean {
        // Get the ConnectivityManager system service
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // Check if the device has an active network
        val network = connectivityManager.activeNetwork ?: return false
        // Get the network capabilities for the active network
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        // Check if the network has any of the following transports:
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

}