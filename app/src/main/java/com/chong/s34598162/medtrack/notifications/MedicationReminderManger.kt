package com.chong.s34598162.medtrack.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import com.chong.s34598162.medtrack.data.Medication.Medication

/**
 * Singleton object responsible for managing medication reminder alarms.
 *
 * Handles three main operations:
 * - Creating the notification channel required to display reminders.
 * - Scheduling a daily repeating alarm for each medication at its scheduled time.
 * - Cancelling an alarm when a medication is deleted.
 *
 * Each medication is assigned its own unique [PendingIntent] identified by
 * its [Medication.medicationId], ensuring alarms do not overwrite each other.
 */
object MedicationReminderManager {
    // Notification channel ID used for all medication reminder notifications.
    val channelId = "medication_reminder"

    // Creates the notification channel for medication reminders.
    // Must be called before any notification can be displayed.
    // Safe to call multiple times — the system ignores duplicate channel creation.
    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            channelId,
            "Medication Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminds you to take you medication on time"
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    /**
     * Schedules a daily repeating alarm for the given medication.
     *
     * The alarm fires at the medication's [Medication.scheduledTime] each day.
     * If the scheduled time has already passed today, the first trigger is
     * pushed to the same time tomorrow.
     *
     * Uses [AlarmManager.setInexactRepeating] with a 24-hour interval,
     * which allows the system to batch alarms for battery efficiency.
     *
     * @param context    Application context used to access [AlarmManager].
     * @param medication The medication to schedule a reminder for.
     *                   Skipped silently if [Medication.scheduledTime] cannot be parsed
     *                   or if [Medication.medicationId] is 0 (not yet assigned by Room).
     */
    fun scheduleReminder(context: Context, medication: Medication) {
        val (hour, minute) = parseTime(medication.scheduledTime) ?: return // skip if time cannot parse
        val pendingIntent: PendingIntent = buildPendingIntent(context, medication) ?: return

        val triggerAt = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR,1)
            }
        }.timeInMillis

        context.getSystemService(AlarmManager::class.java).setInexactRepeating(
            AlarmManager.RTC_WAKEUP, // wake device if screen off
            triggerAt,
            AlarmManager.INTERVAL_DAY, // repeat every 24 hour
            pendingIntent
        )
    }

    /**
     * Cancels the scheduled alarm for the given medication.
     * Should be called whenever a medication is deleted to prevent
     * orphaned reminders from firing after the record no longer exists.
     *
     * @param context      Application context used to access [AlarmManager].
     * @param medicationId The ID of the medication whose alarm should be cancelled.
     */
    fun cancelReminder(context: Context, medicationId: Int) {
        val intent = Intent(context, MedicationReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        context.getSystemService(AlarmManager::class.java).cancel(pendingIntent)
        pendingIntent.cancel()
    }

    // Parses "HH:mm" → (hour, minute). Returns null if the format is unrecognised.
    private fun parseTime(scheduledTime: String): Pair<Int, Int>? {
        return try {
            val parts = scheduledTime.trim().split(":")
            Pair(parts[0].toInt(), parts[1].toInt())
        } catch (e: Exception) {
            null
        }
    }

    // Each medication gets its own PendingIntent identified by its unique medicationId
    private fun buildPendingIntent(context: Context, medication: Medication): PendingIntent? {
        if (medication.medicationId == 0) return null  // ID not yet assigned — skip
        val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
            putExtra("medication_id", medication.medicationId)
            putExtra("medication_name", medication.name)
            putExtra("medication_dosage", medication.dosage)
        }
        return PendingIntent.getBroadcast(
            context,
            medication.medicationId,  // unique request code ensures each med has its own alarm
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

}