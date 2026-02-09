package com.example.todolist.ui.feature.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todolist.domain.Todo
import com.example.todolist.domain.todo1
import com.example.todolist.domain.todo2
import com.example.todolist.domain.todo3
import com.example.todolist.navigation.AddEditRoute
import com.example.todolist.ui.UiEvent
import com.example.todolist.ui.auth.AuthViewModel
import com.example.todolist.ui.components.TodoItem
import com.example.todolist.ui.theme.ToDoListTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    navigateToAddEditScreen: (id: String?) -> Unit,
    onSignOut: () -> Unit,
    viewModel: ListViewModel = hiltViewModel(),
    authViewModel: AuthViewModel
) {
    val todos by viewModel.todos.collectAsStateWithLifecycle()
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var hasShownWelcome by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { uiEvent ->
            when (uiEvent) {
                is UiEvent.Navigate<*> -> {
                    when (val route = uiEvent.route) {
                        is AddEditRoute -> {
                            navigateToAddEditScreen(route.id)
                        }
                    }
                }
                UiEvent.NavigateBack -> {}
                is UiEvent.showSnackbar -> {
                    snackbarHostState.showSnackbar(uiEvent.message)
                }
            }
        }
    }


    LaunchedEffect(authUiState.isAuthenticated) {
        if (!authUiState.isAuthenticated) {
            onSignOut()
            hasShownWelcome = false
        } else if (!hasShownWelcome) {
            hasShownWelcome = true
            val email = authUiState.user?.email ?: "Usuário"
            // Exibe a mensagem de sucesso imediatamente e garante 5s de exibição
            launch {
                snackbarHostState.showSnackbar(
                    message = "Logado com sucesso!",
                    withDismissAction = false,
                    duration = SnackbarDuration.Indefinite
                )
            }
            // Aguarda 5s e troca pela mensagem de boas-vindas
            delay(5000)
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar("Bem-vindo, $email!")
        }
    }

    ListContent(
        todos = todos,
        onEvent = viewModel::onEvent,
        onSignOutClick = { authViewModel.onEvent(com.example.todolist.ui.auth.AuthEvent.SignOut) },
        userEmail = authUiState.user?.email ?: "Usuário",
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListContent(
    todos: List<Todo>,
    onEvent: (ListEvent) -> Unit,
    onSignOutClick: () -> Unit,
    userEmail: String,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Minhas Tarefas")
                        Text(
                            text = userEmail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSignOutClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sign Out"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onEvent(ListEvent.AddEdit(null))
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        if (todos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Nenhuma tarefa",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Clique em '+' para criar tarefa",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp)
            ) {
                itemsIndexed(todos) { index, todo ->
                    TodoItem(
                        todo = todo,
                        onCompletedChange = {
                            onEvent(ListEvent.CompleteChanged(todo.id, it))
                        },
                        onItemClick = {
                            onEvent(ListEvent.AddEdit(todo.id))
                        },
                        onDeleteClick = {
                            onEvent(ListEvent.Delete(todo.id))
                        }
                    )

                    if (index < todos.lastIndex) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ListContentPreview() {
    ToDoListTheme {
        ListContent(
            todos = listOf(todo1, todo2, todo3),
            onEvent = {},
            onSignOutClick = {},
            userEmail = "user@example.com",
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Preview
@Composable
private fun ListContentEmptyPreview() {
    ToDoListTheme {
        ListContent(
            todos = emptyList(),
            onEvent = {},
            onSignOutClick = {},
            userEmail = "user@example.com",
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}