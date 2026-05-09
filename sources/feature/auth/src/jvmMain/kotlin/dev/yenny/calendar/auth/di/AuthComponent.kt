package dev.yenny.calendar.auth.di

import dev.yenny.calendar.auth.data.GoogleAuthCodeRemoteDataSource
import dev.yenny.calendar.auth.data.GoogleAuthCodeRemoteDataSourceImpl
import dev.yenny.calendar.auth.data.GoogleAuthTokenRemoteDataSourceImpl
import dev.yenny.calendar.auth.data.GoogleProfileRemoteDataSource
import dev.yenny.calendar.auth.data.GoogleProfileRemoteDataSourceImpl
import dev.yenny.calendar.auth.data.TokensLocalDataSource
import dev.yenny.calendar.auth.data.TokensLocalDataSourceImpl
import dev.yenny.calendar.auth.data.UsersLocalDataSource
import dev.yenny.calendar.auth.data.UsersLocalDataSourceImpl
import dev.yenny.calendar.auth.domain.GoogleAuthRepository
import dev.yenny.calendar.auth.domain.GoogleAuthRepositoryImpl
import dev.yenny.calendar.auth.domain.GoogleUserRepository
import dev.yenny.calendar.auth.domain.GoogleUserRepositoryImpl
import dev.yenny.calendar.auth.domain.LogInGoogleAccountUseCase
import dev.yenny.calendar.auth.domain.LogInGoogleAccountUseCaseImpl
import dev.yenny.calendar.di.lazySafePublication
import dev.yenny.calendar.di.safePublicationReusable

interface AuthComponent {

    val googleUserRepository: Lazy<GoogleUserRepository>

    val logInGoogleAccountUseCase: LogInGoogleAccountUseCase

    companion object {

        val instance: AuthComponent by safePublicationReusable { AuthComponentImpl() }
    }
}

internal class AuthComponentImpl(
    dependencies: AuthDependencies = AuthDependencies,
) : AuthComponent, AuthDependencies by dependencies {

    private val googleAuthCodeRemoteDataSource: Lazy<GoogleAuthCodeRemoteDataSource>
        get() = lazySafePublication { GoogleAuthCodeRemoteDataSourceImpl() }

    private val googleAuthTokenRemoteDataSource: Lazy<GoogleAuthTokenRemoteDataSourceImpl>
        get() = lazySafePublication { GoogleAuthTokenRemoteDataSourceImpl(client = httpClient) }

    private val googleProfileRemoteDataSource: Lazy<GoogleProfileRemoteDataSource>
        get() = lazySafePublication {
            GoogleProfileRemoteDataSourceImpl(client = httpClient)
        }

    private val usersLocalDataSource: Lazy<UsersLocalDataSource>
        get() = lazySafePublication { UsersLocalDataSourceImpl(database = database) }

    private val tokensLocalDataSource: Lazy<TokensLocalDataSource>
        get() = lazySafePublication { TokensLocalDataSourceImpl(database = database) }

    private val googleAuthRepository: Lazy<GoogleAuthRepository> = safePublicationReusable {
        GoogleAuthRepositoryImpl(
            ioDispatcher = ioDispatcher,
            userRepository = googleUserRepository,
            codeDataSource = googleAuthCodeRemoteDataSource,
            tokenDataSource = googleAuthTokenRemoteDataSource,
            profileDataSource = googleProfileRemoteDataSource,
        )
    }

    override val googleUserRepository: Lazy<GoogleUserRepository> = lazySafePublication {
        GoogleUserRepositoryImpl(
            ioDispatcher = ioDispatcher,
            usersDataSource = usersLocalDataSource,
            tokensDataSource = tokensLocalDataSource,
        )
    }

    override val logInGoogleAccountUseCase: LogInGoogleAccountUseCase
        get() = LogInGoogleAccountUseCaseImpl(repository = googleAuthRepository)
}
