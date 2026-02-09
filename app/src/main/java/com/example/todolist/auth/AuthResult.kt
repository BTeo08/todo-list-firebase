package com.example.todolist.auth

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()

}