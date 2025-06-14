package com.example.demotodo.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.demotodo.model.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE username = :username AND password = :password")
    fun login(username: String, password: String): User?
}