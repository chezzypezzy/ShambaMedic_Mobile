package com.example.shambamedic.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shambamedic.data.repository.UserRepository
import com.example.shambamedic.domain.usecase.AuthenticateUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoginMode: Boolean = true,
    val name: String = "",
    val phoneNumber: String = "",
    val pin: String = "",
    val confirmPin: String = "",
    val nameError: String? = null,
    val phoneError: String? = null,
    val pinError: String? = null,
    val confirmPinError: String? = null,
    val generalError: String? = null,
    val isAuthenticated: Boolean = false,
    val selectedLanguage: String = "en",
    val isGoogleSignInLoading: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authenticateUserUseCase: AuthenticateUserUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser()
            if (user != null) {
                _uiState.update { it.copy(isAuthenticated = true) }
            }
        }
    }

    fun toggleMode() {
        _uiState.update { 
            it.copy(
                isLoginMode = !it.isLoginMode,
                name = "",
                phoneNumber = "",
                pin = "",
                confirmPin = "",
                nameError = null,
                phoneError = null,
                pinError = null,
                confirmPinError = null,
                generalError = null
            ) 
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, nameError = null) }
    }

    fun onPhoneChange(value: String) {
        if (value.length <= 10 && value.all { it.isDigit() }) {
            _uiState.update { it.copy(phoneNumber = value, phoneError = null) }
        }
    }

    fun onPinChange(value: String) {
        if (value.length <= 4 && value.all { it.isDigit() }) {
            _uiState.update { it.copy(pin = value, pinError = null) }
        }
    }

    fun onConfirmPinChange(value: String) {
        if (value.length <= 4 && value.all { it.isDigit() }) {
            _uiState.update { it.copy(confirmPin = value, confirmPinError = null) }
        }
    }

    fun onLanguageChange(language: String) {
        _uiState.update { it.copy(selectedLanguage = language) }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        val state = _uiState.value

        if (state.phoneNumber.isBlank()) {
            _uiState.update { it.copy(phoneError = "Phone number required") }
            isValid = false
        }

        if (state.pin.length != 4) {
            _uiState.update { it.copy(pinError = "4-digit PIN required") }
            isValid = false
        }

        if (!state.isLoginMode) {
            if (state.name.isBlank()) {
                _uiState.update { it.copy(nameError = "Full name required") }
                isValid = false
            }
            if (state.confirmPin != state.pin) {
                _uiState.update { it.copy(confirmPinError = "PINs do not match") }
                isValid = false
            }
        }

        return isValid
    }

    fun authenticate() {
        if (!validateInputs()) return

        _uiState.update { it.copy(isLoading = true, generalError = null) }

        viewModelScope.launch {
            val state = _uiState.value
            val result = authenticateUserUseCase(
                phoneNumber = state.phoneNumber,
                pin = state.pin,
                isRegistering = !state.isLoginMode,
                name = state.name
            )

            result.onSuccess {
                _uiState.update { it.copy(isAuthenticated = true, isLoading = false) }
            }.onFailure { error ->
                _uiState.update { it.copy(generalError = error.message, isLoading = false) }
            }
        }
    }

    fun onGoogleSignInResult(
        googleId: String,
        email: String,
        displayName: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGoogleSignInLoading = true) }
            val result = userRepository.signInWithGoogle(googleId, email, displayName)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(isAuthenticated = true, isGoogleSignInLoading = false)
                }
            } else {
                _uiState.update {
                    it.copy(
                        generalError = "Google sign-in failed. Please try again.",
                        isGoogleSignInLoading = false
                    )
                }
            }
        }
    }

    fun onGoogleSignInError(message: String) {
        _uiState.update {
            it.copy(generalError = message, isGoogleSignInLoading = false)
        }
    }
}
