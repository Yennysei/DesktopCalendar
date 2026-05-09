package dev.yenny.calendar.auth.domain.usecase

import dev.yenny.calendar.auth.domain.model.AuthStatus
import dev.yenny.calendar.auth.domain.repository.GoogleAuthRepository
import dev.yenny.calendar.di.invoke
import kotlinx.coroutines.flow.Flow

interface LogInGoogleAccountUseCase: () -> Flow<AuthStatus>

internal class LogInGoogleAccountUseCaseImpl(
    private val repository: Lazy<GoogleAuthRepository>,
) : LogInGoogleAccountUseCase {

    override fun invoke(): Flow<AuthStatus> {
        return repository()
            .getAuthenticationStatusFlow()
    }
}
