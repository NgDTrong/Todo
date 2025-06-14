package com.example.demotodo.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.demotodo.model.Notification

@Dao
interface NotificationDAO {
    @Insert
    fun insert(notification: Notification)

    @Query("SELECT * FROM nonotifications ORDER BY timestamp DESC")
    fun getAll(): List<Notification>

    @Query("DELETE FROM nonotifications")
    fun clearAll()
}
