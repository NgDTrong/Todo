package com.example.demotodo.reponsitory

import com.example.demotodo.DAO.UserDao
import com.example.demotodo.model.User

class UserRepository(private val userDao: UserDao) {
    fun insertUser(user: User): Boolean {
        return try {
            userDao.insertUser(user)
            true
        } catch (e: Exception) {
            false
        }
    }

  fun login(username: String, password: String) = userDao.login(username, password)
}