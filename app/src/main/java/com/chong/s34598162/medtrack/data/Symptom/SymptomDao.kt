package com.chong.s34598162.medtrack.data.Symptom

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Data Access Object (DAO) for the [Symptom] entity.
 *
 * Provides database operations for logging and retrieving patient symptoms,
 * as well as aggregate queries used by the Clinician Dashboard.
 */
@Dao
interface SymptomDao {
    // Inserts a new symptom log entry into the database.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSymptom(symptom: Symptom)

//    // Fetches all symptoms for a specific patient
//    @Query("SELECT * FROM symptoms WHERE patientId = :patientId ORDER BY dateTime DESC")
//    suspend fun getSymptomsByPatientIdOnce(patientId: String): List<Symptom>

    // Fetches all symptoms for a specific patient as a regular (non-suspend) call.
    @Query("SELECT * FROM symptoms WHERE patientId = :patientId ORDER BY dateTime DESC")
    fun getSymptomsByPatientId(patientId: String): List<Symptom>

    // Returns the symptom category that has been logged most frequently
    @Query("SELECT category FROM symptoms GROUP BY category ORDER BY COUNT(*) DESC LIMIT 1")
    suspend fun getMostCommonSymptomCategory(): String?

    // Calculates the average severity score across all recorded symptoms,
    @Query("SELECT AVG(severity) FROM symptoms")
    suspend fun  getAverageSymptomSeverity(): Double
}