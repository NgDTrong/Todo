package com.example.demotodo.repository

import com.example.demotodo.DAO.TodoDAO
import com.example.demotodo.TodoActivity
import com.example.demotodo.data.AppDatabase
import com.example.demotodo.model.Todo

class TodoRepository(private val todoDao: TodoDAO) {
    fun getAllTodosByUser(userId: Int): List<Todo> {
        return todoDao.getAllByUser(userId)
    }
    fun searchTodos(userId: Int, keyword: String): List<Todo> {
        return todoDao.searchTodo(userId, keyword)
    }
    fun insertTodo(todo: Todo) {
        todoDao.insertTodo(todo)
    }

    fun deleteTodo(todo: Todo) {
        todoDao.deleteTodo(todo)
    }

    fun updateTodo(todo: Todo) {
        todoDao.updateTodo(todo)
    }
}
