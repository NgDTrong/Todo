package com.example.demotodo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.demotodo.DAO.TodoDAO

class UpdateTodoViewModelFactory(private val todoDAO: TodoDAO): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UpdateTodoViewModel::class.java)) {
            return UpdateTodoViewModel(todoDAO) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}