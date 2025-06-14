package com.example.demotodo.service

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demotodo.DAO.NotificationDAO
import com.example.demotodo.R
import com.example.demotodo.adapter.NotificationAdapter
import com.example.demotodo.data.AppDatabase

class NotificationActivity : AppCompatActivity() {
    private lateinit var nottificationDao: NotificationDAO
    private lateinit var adapter: NotificationAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notification)

        val recyclerView = findViewById<RecyclerView>(R.id.rv_notification)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val notificationDAO = AppDatabase.getDatabase(this).notificationDAO()
        val notifications = notificationDAO.getAll()

        adapter = NotificationAdapter(notifications)
        recyclerView.adapter = adapter
        findViewById<ImageView>(R.id.img_back).setOnClickListener {
            finish()
        }
    }
}