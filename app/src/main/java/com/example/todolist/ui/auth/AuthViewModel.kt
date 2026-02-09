package com.example.todolist.ui.auth


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.auth.AuthRepository
import com.example.todolist.auth.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isActionLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _successMessage = MutableStateFlow<String?>(null)


    val uiState: StateFlow<AuthUiState> = combine(
        authRepository.authState,
        _isActionLoading,
        _errorMessage,
        _successMessage
    ) { authState, isActionLoading, errorMessage, successMessage ->
        AuthUiState(
            isAuthenticated = authState.isAuthenticated,
            isInitialLoading = authState.isLoading,
            isActionLoading = isActionLoading,
            errorMessage = errorMessage,
            successMessage = successMessage,
            user = authState.user
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AuthUiState()
    )

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.SignIn -> signIn(event.email, event.password)
            is AuthEvent.SignUp -> signUp(event.email, event.password)
            is AuthEvent.ResetPassword -> resetPassword(event.email)
            is AuthEvent.SignOut -> signOut()
            is AuthEvent.ClearError -> clearMessages()
        }
    }

    private fun signIn(email: String, password: String) {
        viewModelScope.launch {
            if (!isValidEmail(email)) {
                _errorMessage.value = "Entre com um e-mail válido."
                return@launch
            }
            if (password.length < 6) {
                _errorMessage.value = "A senha precisa ter pelo menos 6 caracteres."
                return@launch
            }

            _isActionLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null


            when (val result = authRepository.signIn(email, password)) {
                is AuthResult.Success -> {
                    // Login bem-sucedido. A mensagem de sucesso será mostrada na ListScreen.
                }


                is AuthResult.Error -> {
                    _errorMessage.value = result.message
                }
            }
            _isActionLoading.value = false
        }
    }

    private fun signUp(email: String, password: String) {
        viewModelScope.launch {
            if (!isValidEmail(email)) {
                _errorMessage.value = "Entre com um e-mail válido."
                return@launch
            }
            if (password.length < 6) {
                _errorMessage.value = "A senha precisa ter pelo menos 6 caracteres."
                return@launch
            }

            _isActionLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            when (val result = authRepository.signUp(email, password)) {
                is AuthResult.Success -> {
                    _successMessage.value = "Cadastro feito com sucesso!"
                }

                is AuthResult.Error -> {
                    _errorMessage.value = result.message
                }
            }
            _isActionLoading.value = false
        }
    }

    private fun resetPassword(email: String) {
        viewModelScope.launch {
            if (!isValidEmail(email)) {
                _errorMessage.value = "Entre com um e-mail válido."
                return@launch
            }

            _isActionLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            when (val result = authRepository.resetPassword(email)) {
                is AuthResult.Success -> {
                    _successMessage.value = "E-mail para redefinição de senha enviado!"
                }

                is AuthResult.Error -> {
                    _errorMessage.value = result.message
                }
            }
            _isActionLoading.value = false
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            _isActionLoading.value = true
            authRepository.signOut()
            _isActionLoading.value = false
        }
    }

    private fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}