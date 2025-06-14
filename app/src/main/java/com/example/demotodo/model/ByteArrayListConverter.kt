package com.example.demotodo.model

import androidx.room.TypeConverter
import android.util.Base64

class ByteArrayListConverter {
    @TypeConverter
    fun fromByteArrayList(value: List<ByteArray>?): String? {
        return value?.joinToString("|") { Base64.encodeToString(it, Base64.DEFAULT) }
    }

    @TypeConverter
    fun toByteArrayList(value: String?): List<ByteArray>? {
        return value?.split("|")?.mapNotNull {
            try {
                Base64.decode(it, Base64.DEFAULT)
            } catch (e: Exception) {
                null
            }
        }
    }
}