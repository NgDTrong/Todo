package com.example.demotodo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demotodo.DAO.TodoDAO
import com.example.demotodo.model.Todo
import kotlinx.coroutines.launch

class UpdateTodoViewModel(private val todoDAO: TodoDAO): ViewModel() {
    private val _todo =MutableLiveData<Todo>()
    val todo:LiveData<Todo> get()=_todo
    fun loadTodoById(id:Int){
        viewModelScope.launch {
            _todo.postValue(todoDAO.getTodoById(id))
        }
    }
    fun updateTodo(todo: Todo){
        viewModelScope.launch {
            todoDAO.updateTodo(todo)
        }
    }
}