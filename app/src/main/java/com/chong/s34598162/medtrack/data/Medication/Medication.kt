package com.chong.s34598162.medtrack.data.Medication

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.chong.s34598162.medtrack.data.Patient.Patient

/**
 * Room entity representing a single medication record belonging to a patient.
 *
 * - Stored in the "medications" table.
 * - Linked to the [Patient] entity via a foreign key on [patientId].
 *   Deleting a patient automatically deletes all their medications (CASCADE).
 * - [patientId] is indexed to speed up per-patient medication queries.
 */
@Entity(
    tableName = "medications",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = ["patientId"],
        childColumns = ["patientId"],
        onDelete = ForeignKey.CASCADE)],
    indices = [Index("patientId")])
data class Medication(
    @PrimaryKey(autoGenerate = true)
    val medicationId: Int = 0,
    val patientId: String?,
    val name: String,
    val dosage: String,
    val frequency: String,
    val scheduledTime: String,
    val type: String,
    val notes: String,
    val isTaken: Boolean = false
)