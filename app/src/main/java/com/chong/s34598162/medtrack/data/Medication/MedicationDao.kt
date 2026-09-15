package com.chong.s34598162.medtrack.data.Medication

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chong.s34598162.medtrack.data.Patient.Patient
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the [Medication] entity.
 *
 * Provides all database operations for creating, reading, updating,
 * and deleting medication records, as well as aggregate queries
 * used by the Clinician Dashboard.
 */
@Dao
interface MedicationDao {
    // Inserts a new medication into the database.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMedication(medication: Medication): Long

    // Retrieves all medications for a specific patient,
    @Query("SELECT * FROM medications WHERE patientId = :patientId ORDER BY scheduledTime ASC")
    suspend fun getMedicationsByPatientId(patientId: String): List<Medication>

    //  Deletes a medication record from the database.
    @Delete
    suspend fun deleteMedication(medication: Medication)

    // Updates the [Medication.isTaken] flag for a single medication.
    @Query("UPDATE medications SET isTaken = :isTaken WHERE medicationId = :medicationId")
    suspend fun  updateIsTaken(medicationId: Int, isTaken: Boolean)

    //  Resets the [Medication.isTaken] flag to false for all medications
    @Query("UPDATE medications SET isTaken = false WHERE patientId = :patientId")
    suspend fun resetAllTaken(patientId: String)


    // Calculates the average number of medications per patient across all patients.
    @Query("SELECT AVG(med_count) FROM (SELECT COUNT(*) AS med_count FROM medications GROUP BY patientId) ")
    suspend fun getAverageMedicationsPerPatient(): Double
}
