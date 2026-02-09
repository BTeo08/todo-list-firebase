package com.example.todolist.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.todolist.ui.auth.AuthEvent
import com.example.todolist.ui.auth.AuthScreen
import com.example.todolist.ui.auth.AuthViewModel
import com.example.todolist.ui.feature.addedit.AddEditScreen
import com.example.todolist.ui.feature.list.ListScreen
import kotlinx.serialization.Serializable

@Serializable
object AuthRoute

@Serializable
object ListRoute

@Serializable
data class AddEditRoute(val id: String? = null)

@Composable
fun TodoNavHost() {

    val authViewModel: AuthViewModel = hiltViewModel()
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()


    androidx.compose.runtime.LaunchedEffect(uiState) {
        println("DEBUG TodoNavHost - isInitialLoading: ${uiState.isInitialLoading}, isAuthenticated: ${uiState.isAuthenticated}, isActionLoading: ${uiState.isActionLoading}")
    }

    if (uiState.isInitialLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (uiState.isAuthenticated) {
        MainAppNavHost(authViewModel = authViewModel)
    } else {
        AuthNavHost(authViewModel = authViewModel)
    }
}

@Composable
fun AuthNavHost(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = AuthRoute) {
        composable<AuthRoute> {
            AuthScreen(
                onAuthSuccess = {},
                viewModel = authViewModel
            )
        }
    }
}


@Composable
fun MainAppNavHost(authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ListRoute) {
        composable<ListRoute> {
            ListScreen(
                navigateToAddEditScreen = { id ->
                    navController.navigate(AddEditRoute(id = id))
                },
                onSignOut = {
                    authViewModel.onEvent(AuthEvent.SignOut)
                },
                authViewModel = authViewModel
            )
        }
        composable<AddEditRoute> { backStackEntry ->
            val addEditRoute = backStackEntry.toRoute<AddEditRoute>()
            AddEditScreen(
                id = addEditRoute.id,
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}