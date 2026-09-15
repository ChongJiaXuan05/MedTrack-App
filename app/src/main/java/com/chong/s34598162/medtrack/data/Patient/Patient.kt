package com.chong.s34598162.medtrack.data.Patient

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a patient account in the MedTrack application.
 *
 * Stored in the "patients" table. Each patient is uniquely identified
 * by a manually assigned [patientId] (e.g. "P1001")
 */
@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey
    val patientId: String,
    val phoneNumber: String,
    val name: String,
    val password: String
)