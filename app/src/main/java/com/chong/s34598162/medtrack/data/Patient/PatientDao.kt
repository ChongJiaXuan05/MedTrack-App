package com.chong.s34598162.medtrack.data.Patient

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the [Patient] entity.
 *
 * Provides all database operations for patient account management,
 * including registration, login, account claiming, and aggregate
 * queries used by the Clinician Dashboard.
 */
@Dao
interface PatientDao {
    // Inserts a new patient into the database
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPatient(patient: Patient)

    // Observes a patient record as a [Flow], emitting updates whenever the record changes.
    @Query("SELECT * FROM patients WHERE patientId = :patientId")
    fun getPatientById(patientId: String): Flow<Patient?>

    // Fetches a single patient record as a one-shot suspend call (non-reactive).
    @Query("SELECT * FROM patients WHERE patientId = :patientId LIMIT 1")
    suspend fun getPatientByIdOnce(patientId: String): Patient?

    // Looks up a patient by their phone number.
    @Query("SELECT * FROM patients WHERE phoneNumber = :phone LIMIT 1")
    suspend fun getPatientByPhone(phone: String): Patient?

    // Validates login credentials by matching both patient ID and password.
    @Query("SELECT * FROM patients WHERE patientId = :patientId AND password = :password LIMIT 1")
    suspend fun login(patientId: String, password: String): Patient?

    //  Checks whether a phone number is already registered in the database.
    @Query("SELECT COUNT(*) FROM patients WHERE phoneNumber = :phone")
    suspend fun phoneExist(phone: String) : Int

    // Retrieves all patient records from the database.
    @Query("SELECT * FROM patients")
    suspend fun getAllPatients(): List<Patient>

    // Updates the password for a specific patient.
    @Query("UPDATE patients SET password = :password WHERE patientId = :patientId")
    suspend fun updatePassword(patientId: String, password: String)

    // Retrieves all patient IDs from the database.
    @Query("SELECT patientId FROM patients")
    suspend fun getAllPatientIds(): List<String>

    // Returns the total number of patients registered in the database.
    @Query("SELECT COUNT(*) FROM patients")
    suspend fun getPatientCount(): Int
}