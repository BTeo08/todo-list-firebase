package com.example.todolist.ui.feature.list

sealed interface ListEvent {
    data class Delete(val id: String) : ListEvent
    data class CompleteChanged(val id: String, val isCompleted: Boolean) : ListEvent
    data class AddEdit(val id: String?) : ListEvent
}