package com.example.demotodo.viewmodel

import androidx.lifecycle.*
import com.example.demotodo.model.Todo
import com.example.demotodo.repository.TodoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TodoViewModel(private val repository: TodoRepository ,
                    private val userId:Int) : ViewModel() {
    private val _todos = MutableLiveData<List<Todo>>()
    val todos: LiveData<List<Todo>> get() = _todos

    private val _currentPage = MutableLiveData(0)
    val currentPage: LiveData<Int> get() = _currentPage

    private val itemPerPage = 5

    init {
        loadTodos()
    }

    fun loadTodos() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getAllTodosByUser(userId)
            _todos.postValue(result)
        }
    }

    fun search(keyword: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.searchTodos(userId, keyword)
            _todos.postValue(result)
            _currentPage.postValue(0)
        }
    }

    fun delete(todo: Todo) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTodo(todo)
            loadTodos()
        }
    }

    fun insert(todo: Todo) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertTodo(todo)
            loadTodos()
        }
    }

    fun update(todo: Todo) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTodo(todo)
            loadTodos()
        }
    }


    fun nextPage() {
        val totalPage = ((_todos.value?.size ?: 0) - 1) / itemPerPage
        if (_currentPage.value!! < totalPage) {
            _currentPage.value = _currentPage.value!! + 1
        }
    }

    fun previousPage() {
        if (_currentPage.value!! > 0) {
            _currentPage.value = _currentPage.value!! - 1
        }
    }

    fun getNotesForCurrentPage(): List<Todo> {
        val fullList = _todos.value ?: emptyList()
        val fromIndex = _currentPage.value!! * itemPerPage
        val toIndex = minOf(fromIndex + itemPerPage, fullList.size)
        return if (fromIndex < toIndex) fullList.subList(fromIndex, toIndex) else emptyList()
    }

    fun getTotalPages(): Int {
        val fullList = _todos.value ?: emptyList()
        return if (fullList.isEmpty()) 1 else (fullList.size - 1) / itemPerPage + 1
    }
    fun reloadTodosFromDatabase(onDone: () -> Unit) {
        viewModelScope.launch {
            val todos = repository.getAllTodosByUser(userId) // hoặc todoDAO.getAllByUser(userId)
            _todos.postValue(todos)
            onDone()
        }
    }

}
