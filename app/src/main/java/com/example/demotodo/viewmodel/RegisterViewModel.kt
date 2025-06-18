package com.example.demotodo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demotodo.data.AppDatabase
import com.example.demotodo.model.User
import com.example.demotodo.reponsitory.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterViewModel(application: Application) : AndroidViewModel(application){
    private val repository: UserRepository
    private val _registerResult = MutableLiveData<Boolean>()
    val registerResult: LiveData<Boolean> = _registerResult

    init {
        val dao = AppDatabase.getDatabase(application).userDao()
        repository = UserRepository(dao)
    }

    fun registerUser(username: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.insertUser(User(username = username, password = password))
            _registerResult.postValue(success)
        }
    }

}