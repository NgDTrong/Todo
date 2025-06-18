package com.example.demotodo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demotodo.manager.PreferenceManager
import com.example.demotodo.model.User
import com.example.demotodo.reponsitory.UserRepository
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: UserRepository,
    private val prefs: PreferenceManager
) : ViewModel() {

    private val _loginResult = MutableLiveData<User?>()
    val loginResult: LiveData<User?> get() = _loginResult

    fun login(username: String, password: String, remember: Boolean) {
        if (username.isEmpty() || password.isEmpty()) {
            _loginResult.value = null
            return
        }

        viewModelScope.launch {
            val user = repository.login(username, password)
            if (user != null) {
                if (remember) {
                    prefs.save(username, password, true)
                } else {
                    prefs.clear()
                }
            }
            _loginResult.postValue(user)
        }
    }
}
