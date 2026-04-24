package dev.yenny.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.yenny.calendar.auth.domain.model.AuthStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class AuthViewModel(
    private val defaultDispatcher: CoroutineDispatcher,
    private val logInGoogleAccount: () -> Flow<AuthStatus>,
) : ViewModel() {

    private val _stateFlow: MutableStateFlow<AuthScreenState> = MutableStateFlow(AuthScreenState())

    val stateFlow: StateFlow<AuthScreenState> = _stateFlow.asStateFlow()

    fun logInGoogle() {
        viewModelScope.launch(defaultDispatcher) {
            _stateFlow.value = _stateFlow.value.copy(isGoogleButtonBlocked = true)

            logInGoogleAccount()
                .collect { status ->
                    val newState = when (status) {
                        AuthStatus.BROWSER_OPEN -> _stateFlow.value.copy(isGoogleButtonBlocked = false)
                        AuthStatus.ERROR -> _stateFlow.value.copy(isGoogleButtonBlocked = false)
                        AuthStatus.SUCCESS -> _stateFlow.value.copy(isGoogleButtonBlocked = false)
                    }

                    _stateFlow.value = newState
                }
        }
    }
}
