package dev.yenny.calendar.auth.domain

import dev.yenny.calendar.auth.data.GoogleAuthCodeRemoteDataSource
import dev.yenny.calendar.auth.data.AuthCodeStatus
import dev.yenny.calendar.auth.data.GoogleAuthTokenRemoteDataSource
import dev.yenny.calendar.auth.data.GoogleProfileRemoteDataSource
import dev.yenny.calendar.auth.domain.model.AuthStatus
import dev.yenny.calendar.di.invoke
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.Clock

internal interface GoogleAuthRepository {

    fun getAuthenticationStatusFlow(): Flow<AuthStatus>
}

@OptIn(ExperimentalCoroutinesApi::class)
internal class GoogleAuthRepositoryImpl(
    private val ioDispatcher: CoroutineDispatcher,
    private val userRepository: Lazy<GoogleUserRepository>,
    private val codeDataSource: Lazy<GoogleAuthCodeRemoteDataSource>,
    private val tokenDataSource: Lazy<GoogleAuthTokenRemoteDataSource>,
    private val profileDataSource: Lazy<GoogleProfileRemoteDataSource>,
) : GoogleAuthRepository {

    private val logger: Logger = LoggerFactory.getLogger(GoogleAuthRepositoryImpl::class.java)

    override fun getAuthenticationStatusFlow(): Flow<AuthStatus> {
        return codeDataSource()
            .getAuthCodeFlow()
            .map { status ->
                when (status) {
                    is AuthCodeStatus.UrlOpen -> AuthStatus.BROWSER_OPEN
                    is AuthCodeStatus.Error -> finishWithError(status.throwable)
                    is AuthCodeStatus.CodeReceived -> continueAuth(code = status)
                }
            }
            .catch { finishWithError(it) }
            .flowOn(ioDispatcher)
    }

    private suspend fun continueAuth(code: AuthCodeStatus.CodeReceived): AuthStatus {
        val tokenReceivedAt = Clock.System.now()

        val authTokenResponse = tokenDataSource()
            .getAuthToken(
                code = code.code,
                redirectUri = code.redirectUri,
                codeVerifier = code.codeVerifier,
            )

        val userProfile = profileDataSource()
            .getUserProfile(idToken = authTokenResponse.idToken)

        userRepository().saveAndSelectUser(user = userProfile)

        userRepository().saveTokens(
            userId = userProfile.id,
            accessToken = authTokenResponse.accessToken,
            receivedAt = tokenReceivedAt,
            expiresIn = authTokenResponse.expiresIn,
            refreshToken = authTokenResponse.refreshToken,
            scope = authTokenResponse.scope,
            type = authTokenResponse.tokenType,
        )

        return AuthStatus.SUCCESS
    }

    private fun finishWithError(throwable: Throwable): AuthStatus {
        logger.atError()
            .setCause(throwable)
            .log()

        return AuthStatus.ERROR
    }
}
