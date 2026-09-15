package com.chong.s34598162.medtrack.data.Clinician

import android.content.Context
import com.chong.s34598162.medtrack.data.MedTrackDatabase

//Holds the four aggregate statistics displayed on the Clinician Dashboard.
data class ClinicianStatistics(
    val totalPatients: Int,
    val avgMedicationPerPatient: Double,
    val mostCommonSymptom: String,
    val avgSymptomSeverity: Double
)

/**
 * Repository responsible for fetching aggregate (cross-patient) statistics
 * from the Room database for the Clinician Dashboard.
 *
 * This repository queries multiple DAOs and combines their results into
 * a single [ClinicianStatistics] object.
 */
class ClinicianRepository(context: Context) {
    private val db = MedTrackDatabase.getDatabase(context)
    private val patientDao = db.patientDao()
    private val medicationDao = db.medicationDao()
    private val symptomDao = db.symptomDao()

    // Runs all four aggregate queries and returns the results as a [ClinicianStatistics] object.
    //Falls back to "None recorded" if no symptoms have been logged yet.
    suspend fun getAggregateStats(): ClinicianStatistics {
        return ClinicianStatistics(
            totalPatients = patientDao.getPatientCount(),
            avgMedicationPerPatient = medicationDao.getAverageMedicationsPerPatient(),
            mostCommonSymptom = symptomDao.getMostCommonSymptomCategory() ?: "None recorded",
            avgSymptomSeverity = symptomDao.getAverageSymptomSeverity()
        )
    }
}