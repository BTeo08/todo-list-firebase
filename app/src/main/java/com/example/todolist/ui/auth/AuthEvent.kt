package com.example.todolist.ui.auth

sealed interface AuthEvent {
    data class SignIn(val email: String, val password: String) : AuthEvent
    data class SignUp(val email: String, val password: String) : AuthEvent
    data class ResetPassword(val email: String) : AuthEvent
    object SignOut : AuthEvent
    object ClearError : AuthEvent
}

data class AuthUiState(
    val isAuthenticated: Boolean = false,
    val isInitialLoading: Boolean = true,
    val isActionLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val welcomeMessage:String? = null,
    val user: com.google.firebase.auth.FirebaseUser? = null
)