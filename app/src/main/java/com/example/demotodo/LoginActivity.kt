package com.example.demotodo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.demotodo.DAO.UserDao
import com.example.demotodo.data.AppDatabase
import com.example.demotodo.model.Session

class LoginActivity : AppCompatActivity() {
    private val DRAWABLE_END = 2
    private var showOffpass = false
    private lateinit var dao: UserDao

    @SuppressLint("ClickableViewAccessibility", "UnsafeIntentLaunch")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        dao = AppDatabase.getDatabase(this).userDao()
        val edtName = findViewById<EditText>(R.id.edt_name)
        val edtPass = findViewById<EditText>(R.id.edt_pass)
        val btnLogin = findViewById<AppCompatButton>(R.id.btn_login)
        val tvRegister = findViewById<TextView>(R.id.tv_register)
        val cbSave = findViewById<CheckBox>(R.id.cb_save)
        val sharePre = getSharedPreferences("loginPre", MODE_PRIVATE)
        val saveUsername = sharePre.getString("username", "")
        val savePassword = sharePre.getString("password", "")
        val isRemembered = sharePre.getBoolean("remember", false)
        edtName.setText(saveUsername)
        edtPass.setText(savePassword)
        cbSave.isChecked = isRemembered
        edtPass.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = edtPass.compoundDrawables[DRAWABLE_END]
                if (drawableEnd != null
                    && event.rawX >= (edtPass.right - drawableEnd.bounds.width())
                ) {
                    showOffpass = !showOffpass
                    showOnPass(edtPass)
                    edtPass.setSelection(edtPass.text.length)
                    return@setOnTouchListener true
                }
            }
            false
        }
        btnLogin.setOnClickListener {
            val username = edtName.text.toString()
            val password = edtPass.text.toString()
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Không để trống", Toast.LENGTH_SHORT).show()
            } else {
                val user = dao.login(username, password)
                if (user != null) {
                    Session.currentUserId = user.id
                    Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
                    if (cbSave.isChecked) {
                        with(sharePre.edit()) {
                            putString("username", username)
                            putString("password", password)
                            putBoolean("remember", true)
                            apply()
                        }
                    } else {
                        with(sharePre.edit()) {
                            clear()
                            apply()
                        }
                    }
                    intent = Intent(this, TodoActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Thông tin sai", Toast.LENGTH_SHORT).show()
                }
            }
        }
        tvRegister.setOnClickListener {
            intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showOnPass(edtPass: EditText) {
        if (showOffpass) {
            edtPass.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            edtPass.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                ContextCompat.getDrawable(this, R.drawable.ic_eye_open),
                null
            )
        } else {
            edtPass.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            edtPass.setCompoundDrawablesRelativeWithIntrinsicBounds(
                null,
                null,
                ContextCompat.getDrawable(this, R.drawable.ic_eye_close),
                null
            )
        }
    }
}