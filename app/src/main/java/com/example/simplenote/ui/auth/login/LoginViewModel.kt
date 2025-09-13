package com.example.simplenote.ui.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.domain.repository.AuthRepository
import com.example.simplenote.domain.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success.asStateFlow()

    private val _scheme = MutableStateFlow(authRepo.currentScheme())
    val scheme: StateFlow<String> = _scheme.asStateFlow()

    fun updateScheme(s: String) { _scheme.value = s; authRepo.setScheme(s) }

    fun login(username: String, password: String) = viewModelScope.launch {
        _busy.value = true
        _message.value = "Logging in..."
        when (val res = authRepo.login(username, password, _scheme.value)) {
            is AuthResult.Success -> {
                val ui = authRepo.userInfo()
                _message.value = ui.fold(
                    onSuccess = { "Welcome ${it.username}." },
                    onFailure = { "Successful login." }
                )
                _success.value = true
            }
            is AuthResult.Error -> { _message.value = res.message; _success.value = false }
        }
        _busy.value = false
    }
}
