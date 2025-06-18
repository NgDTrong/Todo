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
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.demotodo.DAO.UserDao
import com.example.demotodo.data.AppDatabase
import com.example.demotodo.databinding.ActivityRegisterBinding
import com.example.demotodo.model.User
import com.example.demotodo.reponsitory.UserRepository
import com.example.demotodo.viewmodel.RegisterViewModel
import com.example.demotodo.viewmodel.RegisterViewModelFactory

class RegisterActivity : AppCompatActivity() {
    private val DRAWABLE_END = 2
    private var showOffPass = false
    private lateinit var dao: UserDao
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: RegisterViewModel

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register)
        binding.lifecycleOwner = this
        dao = AppDatabase.getDatabase(this).userDao()
        viewModel = ViewModelProvider(this,RegisterViewModelFactory(application))[RegisterViewModel::class.java]
        binding.viewModel = viewModel
        binding.edtPass.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = binding.edtPass.compoundDrawables[DRAWABLE_END]
                if (drawableEnd != null && event.rawX >= (binding.edtPass.right - drawableEnd.bounds.width())) {
                    showOffPass = !showOffPass
                    showOnPass()
                    binding.edtPass.setSelection(binding.edtPass.text.length)
                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener false
        }
        binding.edtPassAgain.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = binding.edtPassAgain.compoundDrawables[DRAWABLE_END]
                if (drawableEnd != null && event.rawX >= (binding.edtPassAgain.right - drawableEnd.bounds.width())) {
                    showOffPass = !showOffPass
                    showOnPass()
                    binding.edtPassAgain.setSelection(binding.edtPassAgain.text.length)
                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener false
        }
        binding.btnRegister.setOnClickListener {
            val username= binding.edtName.text.toString().trim()
            val pass= binding.edtPass.text.toString().trim()
            val passAgain= binding.edtPassAgain.text.toString().trim()
            if (username.length<4){
                Toast.makeText(this,"Username phải có ít nhất 4 kí tự",Toast.LENGTH_SHORT).show()
            }else if (pass.length<5){
                Toast.makeText(this,"Mật khẩu phải có ít nhất 5 kí tự",Toast.LENGTH_SHORT).show()
            }else if (passAgain!=pass){
                Toast.makeText(this,"Mật khẩu không trùng nhau",Toast.LENGTH_SHORT).show()
            }else{
                viewModel.registerUser(username,pass)
            }
            viewModel.registerResult.observe(this){success->
                if (success){
                    Toast.makeText(this,"Đăng kí thành công",Toast.LENGTH_SHORT).show()
                    finish()
                }else{
                    Toast.makeText(this,"Tên người dùng đã tồn tại",Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun showOnPass() {
        val drawable = if (showOffPass) {
            R.drawable.ic_eye_open
        } else {
            R.drawable.ic_eye_close
        }
        binding.edtPass.inputType = if (showOffPass) {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        binding.edtPass.setCompoundDrawablesWithIntrinsicBounds(
            null,
            null,
            ContextCompat.getDrawable(this, drawable),
            null
        )
    }

}
