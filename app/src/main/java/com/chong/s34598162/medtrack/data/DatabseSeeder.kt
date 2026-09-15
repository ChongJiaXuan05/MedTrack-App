package com.chong.s34598162.medtrack.data

import android.content.Context
import com.chong.s34598162.medtrack.data.Medication.Medication
import com.chong.s34598162.medtrack.data.Patient.Patient
import com.chong.s34598162.medtrack.data.Symptom.Symptom
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object DatabseSeeder {
    suspend fun seedIfNeeded(context: Context, database: MedTrackDatabase){
        val sharePrefs = context.getSharedPreferences("MedTrackPref", Context.MODE_PRIVATE)
        if (sharePrefs.getBoolean("db_seeded", false)) return
        // Seed patients.csv
        try {
            context.assets.open("patients.csv").bufferedReader().useLines { lines ->
                lines.drop(1).forEach { line ->
                    val values = line.split(",")
                    if (values.size >= 4) {
                        database.patientDao().insertPatient(
                            Patient(
                                patientId = values[0].trim(),
                                phoneNumber = values[1].trim(),
                                name = values[2].trim(),
                                password = ""
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {e.printStackTrace()}
        // Seed medications.csv
        try {
            context.assets.open("medications.csv").bufferedReader().useLines { lines ->
                lines.drop(1).forEach { line ->
                    val values = line.split(",")
                    if (values.size >= 6) {
                        database.medicationDao().insertMedication(
                            Medication(
                                patientId = values[0].trim(),
                                name = values[1].trim(),
                                dosage = values[2].trim(),
                                frequency = values[3].trim(),
                                scheduledTime = values[4].trim(),
                                type = values[5].trim(),
                                notes = if (values.size>6) values[6].trim() else ""
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {e.printStackTrace()}
        // Seed symptoms.csv
        try {
            context.assets.open("symptoms.csv").bufferedReader().useLines { lines ->
                lines.drop(1).forEach { line ->
                    val values = line.split(",")
                    if (values.size >= 5) {
                        database.symptomDao().insertSymptom(
                            Symptom(
                                patientId = values[0].trim(),
                                category = values[1].trim(),
                                severity = values[2].trim().toInt() ?: 0,
                                notes = values[3].trim(),
                                dateTime = values[4].trim(),
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {e.printStackTrace()}

        // Migrate SharePreferences patient into database
        try {
            val gson = Gson()
            val userJson = sharePrefs.getString("users", "[]") ?: "[]"
            val type = object : TypeToken<List<Map<String, String>>>() {}.type
            val userList: List<Map<String, String>> = gson.fromJson(userJson,type)
            userList.forEach { user ->
                val id = user["id"]  ?: return@forEach
                val name = user["name"]  ?: return@forEach
                val phone = user["phone"] ?: return@forEach
                val password = user["password"] ?: return@forEach
                database.patientDao().insertPatient(
                    Patient(patientId = id, name = name, phoneNumber =  phone, password = password)
                )
            }

        } catch (e: Exception) {e.printStackTrace()}

        // Migrate SharePreferences medications into database
        try {
            val gson = Gson()
            val medsJson = sharePrefs.getString("medications", "[]") ?: "[]"
            val type = object : TypeToken<List<Map<String, String>>>() {}.type
            val medList: List<Map<String, String>> = gson.fromJson(medsJson,type)
            medList.forEach { med ->
                val patientId = med["id"] ?: return@forEach
                database.medicationDao().insertMedication(
                    Medication(
                        patientId = patientId,
                        name = med["name"] ?: "",
                        dosage = med["dosage"] ?: "",
                        frequency = med["frequency"] ?: "",
                        scheduledTime = med["scheduledTime"] ?: "",
                        type = med["type"] ?: "",
                        notes = med["notes"] ?: ""
                    )
                )
            }
        } catch (e: Exception) {e.printStackTrace()}

        sharePrefs.edit().putBoolean("db_seeded", true).apply()
    }
}