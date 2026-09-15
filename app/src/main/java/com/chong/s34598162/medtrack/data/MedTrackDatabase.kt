package com.chong.s34598162.medtrack.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.chong.s34598162.medtrack.data.MedCoachTips.MedCoachTips
import com.chong.s34598162.medtrack.data.MedCoachTips.MedCoachTipsDao
import com.chong.s34598162.medtrack.data.Medication.Medication
import com.chong.s34598162.medtrack.data.Medication.MedicationDao
import com.chong.s34598162.medtrack.data.Patient.Patient
import com.chong.s34598162.medtrack.data.Patient.PatientDao
import com.chong.s34598162.medtrack.data.Symptom.Symptom
import com.chong.s34598162.medtrack.data.Symptom.SymptomDao

/**
 * The single Room database instance for the MedTrack application.
 *
 * Manages four entities:
 * - [Patient]      : patient account records.
 * - [Medication]   : medications belonging to each patient.
 * - [Symptom]      : symptom log entries recorded by each patient.
 * - [MedCoachTips] : AI-generated medication tips saved per patient.
 */
@Database(
    entities = [Patient::class, Medication::class, Symptom::class, MedCoachTips::class],
    version = 1,
    exportSchema = false
)
abstract class MedTrackDatabase: RoomDatabase() {
    abstract fun patientDao(): PatientDao
    abstract fun medicationDao(): MedicationDao
    abstract fun  symptomDao(): SymptomDao

    abstract fun medCoachTipsDao(): MedCoachTipsDao

    companion object {
        @Volatile
        private var INSTANCE: MedTrackDatabase? = null

        fun getDatabase(context: Context): MedTrackDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    MedTrackDatabase::class.java,
                    "medtrack_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}