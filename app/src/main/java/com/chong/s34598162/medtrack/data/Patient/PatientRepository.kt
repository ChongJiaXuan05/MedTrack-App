package com.chong.s34598162.medtrack.data.Patient

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.chong.s34598162.medtrack.data.MedTrackDatabase
import com.chong.s34598162.medtrack.data.Medication.MedicationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

/**
 * Repository for [Patient] data operations.
 *
 * Acts as a single source of truth between [PatientViewModel] and [PatientDao],
 * abstracting the underlying Room database from the rest of the application.
 */
class PatientRepository(context: Context) {
    private val patientDao = MedTrackDatabase.getDatabase(context).patientDao()

    // Inserts a new patient into the database
    suspend fun insertPatient(patient: Patient) {
        patientDao.insertPatient(patient)
    }

    // Observes a patient record as a [Flow], emitting updates on any change.
    fun getPatientById(patientId: String): Flow<Patient?> {
        return patientDao.getPatientById(patientId)
    }

    // Fetches a patient record
    suspend fun getPatientByIdOnce(patientId: String): Patient? {
        return patientDao.getPatientByIdOnce(patientId)
    }

    // Looks up a patient by their phone number.
    suspend fun getPatientByPhone(phone: String): Patient? {
        return patientDao.getPatientByPhone(phone)
    }

    // Validates login credentials by matching patient ID and password.
    suspend fun login(phone: String, password: String): Patient? {
        return patientDao.login(phone, password)
    }

    // Checks whether a phone number is already registered.
    suspend fun phoneExist(phone: String): Boolean {
        return patientDao.phoneExist(phone) > 0
    }

    // Updates the password for a specific patient.
    suspend fun updatePassword(patientId: String,password: String) {
        patientDao.updatePassword(patientId,password)
    }

    // Retrieves all patient IDs from the database.
    suspend fun getAllPatientIds(): List<String> {
        return patientDao.getAllPatientIds()
    }
}