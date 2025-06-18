package com.example.demotodo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.demotodo.manager.PreferenceManager
import com.example.demotodo.reponsitory.UserRepository

class LoginViewModelFactory(
    private val repository: UserRepository,
    private val prefs: PreferenceManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(repository,prefs) as T
    }
}