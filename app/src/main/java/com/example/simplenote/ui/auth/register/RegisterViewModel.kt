package com.example.simplenote.ui.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.domain.repository.AuthRepository
import com.example.simplenote.domain.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
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

    fun updateScheme(s: String) {
        _scheme.value = s
        authRepo.setScheme(s)
    }

    fun register(username: String, password: String, email: String, firstName: String?, lastName: String?) =
        viewModelScope.launch {
            _busy.value = true
            _message.value = "در حال ایجاد حساب..."
            when (val res = authRepo.register(username, password, email, firstName, lastName, _scheme.value)) {
                is AuthResult.Success -> {
                    _message.value = res.message ?: "ثبت‌نام موفق. لطفاً وارد شوید."
                    _success.value = true   // ← به Login می‌رویم (نه Main)
                }
                is AuthResult.Error -> {
                    _message.value = res.message
                    _success.value = false
                }
            }
            _busy.value = false
        }

}
