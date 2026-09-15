package com.chong.s34598162.medtrack.data.Patient

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Represents the possible outcomes of an account claim attempt.
 *
 * - [Success]        : Identity verified and password set successfully.
 * - [NotFound]       : No patient record found for the given ID.
 * - [PhoneMismatch]  : The provided phone number does not match the record.
 * - [AlreadyClaimed] : The account already has a password set.
 */
enum class ClaimResult {Success, NotFound, PhoneMismatch, AlreadyClaimed}

/**
 * ViewModel for patient account operations.
 *
 * Responsibilities:
 * - Login and credential validation.
 * - Account claiming for pre-seeded patients.
 * - New patient registration via the sign-up screen.
 * - Fetching patient details (name, phone) for display across the app.
 *
 * All database operations are dispatched to [Dispatchers.IO] via [withContext].
 */
class PatientViewModel(context: Context): ViewModel() {
    private val repository = PatientRepository(context)

    // Validates login credentials against the database.
    suspend fun login(patientId: String, password: String): Patient? {
        return withContext(Dispatchers.IO) {
            repository.login(patientId,password)
        }
    }

    // Attempts to claim a pre-seeded patient account by verifying the patient's
    // ID and phone number, then setting a new password.
    suspend fun claimAccount(patientId: String, phoneNumber: String, newPassword: String): ClaimResult =
        withContext(Dispatchers.IO) {
            // Patient ID must exist in the database.
            val patient = repository.getPatientByIdOnce(patientId)
                ?: return@withContext ClaimResult.NotFound

            // Provided phone number must match the record.
            if (patient.phoneNumber.trim() != phoneNumber.trim())
                return@withContext ClaimResult.PhoneMismatch

            // Account must not already have a password (i.e. unclaimed).
            if (patient.password.isNotEmpty())
                return@withContext ClaimResult.AlreadyClaimed

            repository.updatePassword(patientId, newPassword)
            ClaimResult.Success
        }

    // Registers a new patient account by generating the next available patient ID
    suspend fun registerNewUser(name: String, phone: String, password: String): String =
        withContext(Dispatchers.IO) {
            val nextId = generateNextPatientId()
            repository.insertPatient(
                Patient(patientId = nextId, phoneNumber = phone, name = name, password = password)
            )
            nextId
        }

    // Checks whether a phone number is already registered in the database.
    suspend fun phoneExist(phone: String): Boolean =
        withContext(Dispatchers.IO) {
            repository.phoneExist(phone)
        }

    // Checks whether a patient ID exists in the database.
    suspend fun isPatientIdValid(patientId: String): Boolean =
        withContext(Dispatchers.IO) { repository.getPatientByIdOnce(patientId) != null }

    // Validates login credentials and returns a boolean result.
    suspend fun loginByPatientId(patientId: String, password: String): Boolean =
        withContext(Dispatchers.IO) { repository.login(patientId, password) != null }

    // Fetches the display name of a patient.
    suspend fun getPatientName(patientId: String): String =
        withContext(Dispatchers.IO) {
            repository.getPatientByIdOnce(patientId)?.name ?: "Patient"
        }

    // Fetches the phone number of a patient.
    suspend fun getPatientPhone(patientId: String): String =
        withContext(Dispatchers.IO) {
            repository.getPatientByIdOnce(patientId)?.phoneNumber ?: ""
        }

    // Generates the next available patient ID by finding the highest existing
    private suspend fun generateNextPatientId(): String {
        val ids = repository.getAllPatientIds()
        val max = ids.mapNotNull { it.removePrefix("P").toIntOrNull() }.maxOrNull() ?: 1000
        return "P${max + 1}"
    }

    class PatientViewModelFactory(context: Context): ViewModelProvider.Factory {
        private val context = context.applicationContext
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PatientViewModel(context) as T
    }
}