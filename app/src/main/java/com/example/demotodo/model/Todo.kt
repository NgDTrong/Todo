package com.example.demotodo.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.io.Serializable

@Entity(tableName = "todos")
@TypeConverters(ByteArrayListConverter::class)
data class Todo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var title: String,
    var content: String,
    var userId: Int,
    var imageData: List<ByteArray> = emptyList()
) : Serializable