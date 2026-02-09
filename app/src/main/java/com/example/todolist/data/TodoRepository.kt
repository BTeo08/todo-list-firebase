package com.example.todolist.data

import com.example.todolist.domain.Todo
import kotlinx.coroutines.flow.Flow

interface TodoRepository {

    suspend fun insert(title: String, description: String?, id: String? = null) // MODIFICADO

    suspend fun updateCompleted(id: String, isCompleted: Boolean) // MODIFICADO

    suspend fun delete(id: String) // MODIFICADO

    fun getAll(): Flow<List<Todo>>

    suspend fun getBy(id: String): Todo? // MODIFICADO
}