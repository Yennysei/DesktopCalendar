package dev.yenny.auth.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.yenny.calendar.di.safePublicationReusable
import kotlinx.coroutines.Dispatchers

internal interface AuthUiComponent {

    val viewModelFactory: ViewModelProvider.Factory

    companion object {

        val instance: AuthUiComponent by safePublicationReusable { AuthComponentImpl() }
    }
}

private class AuthComponentImpl(
    dependencies: AuthUiDependencies = AuthUiDependencies.Impl,
) : AuthUiComponent, AuthUiDependencies by dependencies {

    override val viewModelFactory: ViewModelProvider.Factory
        get() = viewModelFactory {
            initializer {
                AuthViewModel(
                    defaultDispatcher = Dispatchers.Default,
                    logInGoogleAccount = logInGoogleAccountUseCase,
                )
            }
        }
}
