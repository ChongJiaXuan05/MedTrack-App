package com.chong.s34598162.medtrack.data.Symptom

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.chong.s34598162.medtrack.data.Patient.Patient

/**
 * Room entity representing a single symptom log entry recorded by a patient.
 *
 * - Stored in the "symptoms" table.
 * - Linked to the [Patient] entity via a foreign key on [patientId].
 *   Deleting a patient automatically deletes all their symptom records (CASCADE).
 * - [patientId] is indexed to speed up per-patient symptom queries.
 */
@Entity(
    tableName = "symptoms",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = ["patientId"],
        childColumns = ["patientId"],
        onDelete = ForeignKey.CASCADE)],
    indices = [Index("patientId")])
class Symptom(
    @PrimaryKey(autoGenerate = true)
    val symptomId: Int = 0,
    val patientId: String,
    val category: String,
    val severity: Int,
    val notes: String,
    val dateTime: String
)