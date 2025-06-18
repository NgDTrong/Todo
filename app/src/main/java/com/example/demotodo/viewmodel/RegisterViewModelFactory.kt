package com.example.demotodo.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.demotodo.reponsitory.UserRepository

class RegisterViewModelFactory(
    private val application: Application
) :ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RegisterViewModel(application) as T
    }
}