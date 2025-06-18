package com.example.demotodo.manager

import android.content.Context

class PreferenceManager(context: Context) {
    private val prefs = context.getSharedPreferences("loginPre", Context.MODE_PRIVATE)

    fun save(username: String, password: String, remember: Boolean) {
        prefs.edit().apply {
            putString("username", username)
            putString("password", password)
            putBoolean("remember", remember)
            apply()
        }
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun getUsername() = prefs.getString("username", "") ?: ""
    fun getPassword() = prefs.getString("password", "") ?: ""
    fun isRemembered() = prefs.getBoolean("remember", false)


}
