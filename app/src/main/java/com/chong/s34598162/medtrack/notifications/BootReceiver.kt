package com.chong.s34598162.medtrack.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.chong.s34598162.medtrack.data.MedTrackDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// AlarmManager alarms are wiped when the device reboots.
// This receiver listens for BOOT_COMPLETED and re-schedules all reminders
// for the currently logged-in patient.
class BootReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        CoroutineScope(Dispatchers.IO).launch {
            val prefs = context.getSharedPreferences("MedTrackPref", Context.MODE_PRIVATE)
            val patientId = prefs.getString("logged_in_patient_id", "") ?: ""
            if (patientId.isEmpty()) return@launch  // no one logged in, nothing to reschedule

            val medications = MedTrackDatabase.getDatabase(context)
                .medicationDao()
                .getMedicationsByPatientId(patientId)

            medications.forEach { medication ->
                MedicationReminderManager.scheduleReminder(context, medication)
            }
        }
    }
}