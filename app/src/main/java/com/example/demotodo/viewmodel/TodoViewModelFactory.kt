package com.example.demotodo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.demotodo.repository.TodoRepository

class TodoViewModelFactory
    (private val repository: TodoRepository, private val userId: Int) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TodoViewModel::class.java)) {
                return TodoViewModel(repository,userId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
}