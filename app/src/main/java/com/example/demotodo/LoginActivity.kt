package com.example.demotodo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.view.inputmethod.InputBinding
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
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.demotodo.DAO.UserDao
import com.example.demotodo.data.AppDatabase
import com.example.demotodo.databinding.ActivityLoginBinding
import com.example.demotodo.manager.PreferenceManager
import com.example.demotodo.model.Session
import com.example.demotodo.reponsitory.UserRepository
import com.example.demotodo.viewmodel.LoginViewModel
import com.example.demotodo.viewmodel.LoginViewModelFactory

class LoginActivity : AppCompatActivity() {
    private val DRAWABLE_END = 2
    private var showOffpass = false
    private lateinit var dao: UserDao
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    @SuppressLint("ClickableViewAccessibility", "UnsafeIntentLaunch")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dao = AppDatabase.getDatabase(this).userDao()
        val repository = UserRepository(dao)
        val prefs = PreferenceManager(this)
        val factory = LoginViewModelFactory(repository, prefs)
        viewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]
        val username = binding.edtName.text.toString().trim()
        val password = binding.edtPass.text.toString().trim()
        val remember = binding.cbSave.isChecked
        binding.edtPass.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = binding.edtPass.compoundDrawables[DRAWABLE_END]
                if (drawableEnd != null && event.rawX >= (binding.edtPass.right - drawableEnd.bounds.width())) {
                    showOffpass = !showOffpass
                    showOnPass()
                    binding.edtPass.setSelection(binding.edtPass.text.length)
                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener false
        }
        binding.btnLogin.setOnClickListener {
            val  username = binding.edtName.text.toString().trim()
            val password = binding.edtPass.text.toString().trim()
            val remember = binding.cbSave.isChecked
            viewModel.login(username,password,remember)
        }
        viewModel.loginResult.observe(this) { user ->
            if (user != null) {
                Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
                Session.currentUserId = user.id
                startActivity(Intent(this, TodoActivity::class.java))
            } else {
                Toast.makeText(this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun showOnPass() {
        val drawable = if (showOffpass) {
            R.drawable.ic_eye_open
        } else {
            R.drawable.ic_eye_close
        }
        binding.edtPass.inputType = if (showOffpass) {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        binding.edtPass.setCompoundDrawablesWithIntrinsicBounds(
            null, null, ContextCompat.getDrawable(this, drawable), null
        )
    }
}