package com.example.demotodo.model

import androidx.core.app.NotificationCompat.MessagingStyle.Message
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nonotifications")
data class Notification (
    @PrimaryKey(autoGenerate = true)
    val id: Int=0,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
