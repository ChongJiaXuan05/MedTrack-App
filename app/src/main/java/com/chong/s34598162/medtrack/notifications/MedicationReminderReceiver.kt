package com.chong.s34598162.medtrack.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

/**
 * [BroadcastReceiver] that fires when an AlarmManager medication reminder triggers.
 *
 * Receives the medication's ID, name, and dosage from the intent extras
 * (set in [MedicationReminderManager.buildPendingIntent]) and displays
 * a high-priority notification to prompt the patient to take their medication.
 *
 * Each notification uses [medicationId] as its notification ID, ensuring
 * multiple medications each display their own separate notification
 * rather than overwriting one another.
 *
 * Registered in AndroidManifest.xml as a non-exported receiver.
 */
class MedicationReminderReceiver : BroadcastReceiver() {
    // Called by the system when the scheduled alarm fires.
    //
    // Extracts medication details from the [intent] extras and posts
    // a notification via [NotificationManager]. If [medicationName] is
    // missing from the extras, the receiver exits silently.
    override fun onReceive(context: Context, intent: Intent) {
        val medicationId = intent.getIntExtra("medication_id", 0)
        val medicationName = intent.getStringExtra("medication_name") ?: return
        val medicationDosage = intent.getStringExtra("medication_dosage") ?: ""

        // Build a high-priority notification with an expanded BigText style
        val notification = NotificationCompat.Builder(context, MedicationReminderManager.channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Time to take your medication")
            .setContentText("$medicationName - $medicationDosage")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Don't forget to take your $medicationName ($medicationDosage)."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()


        // Using medicationId as notification ID so each medication
        // gets its own separate notification (they don't overwrite each other)
        context.getSystemService(NotificationManager::class.java)
            .notify(medicationId, notification)
    }
}