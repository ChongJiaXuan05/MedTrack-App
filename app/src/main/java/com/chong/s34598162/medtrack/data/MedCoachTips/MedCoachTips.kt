package com.chong.s34598162.medtrack.data.MedCoachTips

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medcoach_tips")
data class MedCoachTips (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val patientId: String,
    val tip: String,
    val timestamp: Long = System.currentTimeMillis()
)