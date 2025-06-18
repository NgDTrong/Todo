package com.example.demotodo.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.demotodo.model.Todo

@Dao
interface TodoDAO {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertTodo(todo: Todo)

    @Query("SELECT * FROM todos WHERE userId = :userId ")
    fun getAllByUser(userId: Int): List<Todo>

    @Query("SELECT * FROM todos WHERE id = :id")
    fun getTodoById(id: Int): Todo?

    @Delete
    fun deleteTodo(todo: Todo)

    @Update
    fun updateTodo(todo: Todo)

    @Query("SELECT * FROM todos WHERE userId = :userId AND title LIKE '%' || :name || '%'")
    fun searchTodo(userId: Int, name: String): List<Todo>

    @Query("SELECT * FROM todos WHERE userId = :userId ORDER BY id DESC LIMIT :limit OFFSET :offset")
    fun getTodosByPage(userId: Int, limit: Int, offset: Int): List<Todo>

    @Query("SELECT COUNT(*) FROM todos WHERE userId = :userId")
    fun countTodos(userId: Int): Int
}
