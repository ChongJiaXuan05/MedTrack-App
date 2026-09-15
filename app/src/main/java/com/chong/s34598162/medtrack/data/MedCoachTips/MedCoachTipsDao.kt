package com.chong.s34598162.medtrack.data.MedCoachTips

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chong.s34598162.medtrack.data.Patient.Patient

/**
 * Data Access Object (DAO) for the [MedCoachTips] entity.
 *
 * Provides database operations for storing and retrieving AI-generated
 * medication tips associated with individual patients.
 */
@Dao
interface MedCoachTipsDao {
    //Inserts a new AI-generated tip into the database.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTip(tip: MedCoachTips)

    //Retrieves all tips generated for a specific patient, ordered from most recent to oldest.
    @Query("SELECT * FROM medcoach_tips WHERE patientId = :patientId ORDER BY timestamp DESC")
    suspend fun getTipsByPatientId(patientId: String): List<MedCoachTips>
}