package dev.yenny.auth.ui

import dev.yenny.calendar.auth.domain.LogInGoogleAccountUseCase
import dev.yenny.calendar.auth.di.AuthComponent

internal interface AuthUiDependencies {

    val logInGoogleAccountUseCase: LogInGoogleAccountUseCase
        get() = AuthComponent.instance.logInGoogleAccountUseCase

    object Impl : AuthUiDependencies
}
