package com.example.todolist.ui.feature.addedit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.TodoRepository
import com.example.todolist.ui.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val repository: TodoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var title by mutableStateOf("")
        private set

    var description by mutableStateOf<String?>(null)
        private set


    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private var todoId: String? = null

    init {

        savedStateHandle.get<String>("id")?.let { id ->

            if (id != "null") {
                this.todoId = id
                viewModelScope.launch {
                    repository.getBy(id)?.let { todo ->
                        title = todo.title
                        description = todo.description
                    }
                }
            }
        }
    }

    fun onEvent(event: AddEditEvent) {
        when (event) {
            is AddEditEvent.TitleChanged -> {
                title = event.title
            }

            is AddEditEvent.DescriptionChanged -> {
                description = event.description.takeIf { it.isNotBlank() }
            }

            is AddEditEvent.Save -> {
                saveTodo()
            }
        }
    }

    private fun saveTodo() {
        viewModelScope.launch {
            if (title.isBlank()) {

                _uiEvent.emit(UiEvent.showSnackbar("Coloque um título!"))
                return@launch
            }

            try {
                repository.insert(title, description, todoId)
                _uiEvent.emit(UiEvent.NavigateBack)
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.showSnackbar("Falha ao salvar a tarefa: ${e.message}"))
            }
        }
    }
}