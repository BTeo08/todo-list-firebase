package com.example.todolist.auth

import com.google.firebase.auth.FirebaseUser

data class AuthState(
    val isAuthenticated: Boolean = false,
    val user: FirebaseUser? = null,
    val isLoading: Boolean = true
)