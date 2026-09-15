package com.chong.s34598162.medtrack.data.Symptom

import android.content.Context
import com.chong.s34598162.medtrack.data.MedTrackDatabase
import kotlinx.coroutines.flow.Flow

/**
 * Repository for [Symptom] data operations.
 *
 * Acts as a single source of truth between [SymptomViewModel] and [SymptomDao],
 * abstracting the underlying Room database from the rest of the application.
 */
class SymptomRepository(context: Context) {
    private val symptomDao = MedTrackDatabase.getDatabase(context).symptomDao()

    // Inserts a new symptom log entry into the database.
    suspend fun insertSymptom(symptom: Symptom) {
        symptomDao.insertSymptom(symptom)
    }

    // Retrieves all symptoms for the given patient, ordered from most recent to oldest.
    suspend fun getSymptomsByPatientId(patientId: String) : List<Symptom> {
        return symptomDao.getSymptomsByPatientId(patientId)
    }
}