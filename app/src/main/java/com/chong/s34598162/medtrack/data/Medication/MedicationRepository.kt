package com.chong.s34598162.medtrack.data.Medication

import android.content.Context
import com.chong.s34598162.medtrack.data.MedTrackDatabase

/**
* Repository for [Medication] data operations.
*
* Acts as a single source of truth between the [MedicationViewModel]
* and the [MedicationDao], abstracting the underlying Room database
* from the rest of the application.
*/
class MedicationRepository(context: Context) {
    private val medicationDao = MedTrackDatabase.getDatabase(context).medicationDao()

    //
    //
    suspend fun insertMedication(medication: Medication): Long {
        return medicationDao.insertMedication(medication)
    }

    // Retrieves all medications for the given patient
    suspend fun getMedicationsByPatientId(patientId: String) : List<Medication> {
        return medicationDao.getMedicationsByPatientId(patientId)
    }

    // Deletes a medication from the database.
    suspend fun deleteMedication(medication: Medication) {
        medicationDao.deleteMedication(medication)
    }

    // Updates the taken status of a single medication.
    suspend fun updateIsTaken(medicationId: Int, isTaken: Boolean) {
        medicationDao.updateIsTaken(medicationId, isTaken)
    }

    // Resets the taken status of all medications for the given patient to false.
    suspend fun resetAllTaken(patientId: String) {
        medicationDao.resetAllTaken(patientId)
    }

}