package com.example.demotodo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.example.demotodo.DAO.UserDao
import com.example.demotodo.data.AppDatabase
import com.example.demotodo.model.User

class RegisterActivity : AppCompatActivity() {
    private val DRAWABLE_END = 2
    private var showOffPass = false
    private lateinit var dao: UserDao

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        dao = AppDatabase.getDatabase(this).userDao()
        val edtName = findViewById<EditText>(R.id.edt_name)
        val edtPass = findViewById<EditText>(R.id.edt_pass)
        val edtPassagain = findViewById<EditText>(R.id.edt_pass_again)
        val btnRegister = findViewById<AppCompatButton>(R.id.btn_register)
        val tvLogin = findViewById<TextView>(R.id.tv_login)
        edtPass.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = edtPass.compoundDrawables[DRAWABLE_END]
                if (drawableEnd != null &&
                    event.rawX >= (edtPass.right - drawableEnd.bounds.width())
                ) {
                    showOffPass = !showOffPass
                    showOnPass(edtPass)
                    edtPass.setSelection(edtPass.text.length)
                    return@setOnTouchListener true
                }
            }
            false
        }
        edtPassagain.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = edtPass.compoundDrawables[DRAWABLE_END]
                if (drawableEnd != null &&
                    event.rawX >= (edtPass.right - drawableEnd.bounds.width())
                ) {
                    showOffPass = !showOffPass
                    showOnPassAgain(edtPassagain)
                    edtPassagain.setSelection(edtPassagain.text.length)
                    return@setOnTouchListener true
                }
            }
            false
        }
        btnRegister.setOnClickListener {
            val username = edtName.text.toString().trim()
            val password = edtPass.text.toString().trim()
            val passagain = edtPassagain.text.toString().trim()
            if (username.length < 4) {
                Toast.makeText(this, "Username phải có ít nhất 4 kí tự", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 6) {
                Toast.makeText(this, "Password phải có ít nhất 6 kí tự", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (passagain != password) {
                Toast.makeText(this, "Password phải giống nhau", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val user = User(username = username, password = password)
            try {
                dao.insertUser(user)
                Toast.makeText(this, "Đăng kí thành công...", Toast.LENGTH_SHORT).show()
                finish()

            } catch (e: Exception) {
                Toast.makeText(this, "Tên người dùng đã tồn tại...", Toast.LENGTH_SHORT).show()
            }
        }
        tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showOnPass(edtPass: EditText) {
        if (showOffPass) {
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
            edtPass.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                ContextCompat.getDrawable(this, R.drawable.ic_eye_close),
                null
            )
        }
    }

    private fun showOnPassAgain(edtPassagain: EditText) {
        if (showOffPass) {
            edtPassagain.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            edtPassagain.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                ContextCompat.getDrawable(this, R.drawable.ic_eye_open),
                null
            )
        } else {
            edtPassagain.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            edtPassagain.setCompoundDrawablesRelativeWithIntrinsicBounds(
                null,
                null,
                ContextCompat.getDrawable(this, R.drawable.ic_eye_close),
                null
            )
        }
    }
}
