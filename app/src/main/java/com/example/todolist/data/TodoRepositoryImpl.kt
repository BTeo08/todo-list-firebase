package com.example.todolist.data

import com.example.todolist.domain.Todo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TodoRepositoryImpl(
    private val dao: TodoDao
) : TodoRepository {

    override suspend fun insert(title: String, description: String?, id: String?) {
        val entity = id?.let{
            dao.getBy(it)?.copy(
                    title = title,
                    description = description,
                )
        } ?: TodoEntity(
            title = title,
            description = description,
            isCompleted = false,
        )
        dao.insert(entity)
    }

    override suspend fun updateCompleted(id: String, isCompleted: Boolean) {
        val existingEntity = dao.getBy(id) ?: return
        val updatedEntity = existingEntity.copy(isCompleted = isCompleted)

        dao.insert(updatedEntity)
    }

    override suspend fun delete(id: String) {
        val existingEntity = dao.getBy(id) ?: return
        dao.delete(existingEntity)

    }

    override fun getAll(): Flow<List<Todo>> {
        return dao.getAll().map { entities ->
            entities.map { entity ->
                Todo(
                    id = entity.id.toString(),
                    title = entity.title,
                    description = entity.description,
                    isCompleted = entity.isCompleted,
                )
            }
        }
    }

    override suspend fun getBy(id: String): Todo? {
        return dao.getBy(id)?.let { entity ->
            Todo(
                id = entity.id.toString(),
                title = entity.title,
                description = entity.description,
                isCompleted = entity.isCompleted,
            )
        }

    }
}