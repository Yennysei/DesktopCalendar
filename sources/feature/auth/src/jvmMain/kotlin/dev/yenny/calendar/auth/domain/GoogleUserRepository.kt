package dev.yenny.calendar.auth.domain

import dev.yenny.calendar.auth.data.TokensLocalDataSource
import dev.yenny.calendar.auth.data.UsersLocalDataSource
import dev.yenny.calendar.auth.domain.model.GoogleTokens
import dev.yenny.calendar.auth.domain.model.GoogleUser
import dev.yenny.calendar.di.invoke
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Instant

interface GoogleUserRepository {

    val usersFlow: Flow<List<GoogleUser>>

    suspend fun selectUser(user: GoogleUser)

    suspend fun saveAndSelectUser(user: GoogleUser)

    suspend fun getCurrentUserTokens(): GoogleTokens?

    suspend fun saveTokens(
        userId: String,
        accessToken: String,
        receivedAt: Instant,
        expiresIn: Int,
        refreshToken: String,
        scope: String,
        type: String,
    )
}

@OptIn(ExperimentalCoroutinesApi::class)
internal class GoogleUserRepositoryImpl(
    private val ioDispatcher: CoroutineDispatcher,
    private val usersDataSource: Lazy<UsersLocalDataSource>,
    private val tokensDataSource: Lazy<TokensLocalDataSource>,
) : GoogleUserRepository {

    @Volatile
    private var currentTokens: GoogleTokens? = null

    private val tokensMutex: Mutex = Mutex()

    private val mutableUsersFlow: MutableStateFlow<List<GoogleUser>?> = MutableStateFlow(value = null)

    override val usersFlow: Flow<List<GoogleUser>> = flow {
        mutableUsersFlow.collect { users ->
            if (users == null) {
                mutableUsersFlow.value = usersDataSource()
                    .getCachedUsers()

                return@collect
            }

            emit(value = users)
        }
    }

    override suspend fun selectUser(user: GoogleUser) {
        withContext(ioDispatcher) {
            val users = usersDataSource()
                .selectAndGetUsers(selectedUser = user)

            currentTokens = null
            mutableUsersFlow.value = users
        }
    }

    override suspend fun saveAndSelectUser(user: GoogleUser) {
        withContext(ioDispatcher) {
            val users = usersDataSource()
                .saveAndSelectUser(user = user)

            mutableUsersFlow.value = users
        }
    }

    override suspend fun getCurrentUserTokens(): GoogleTokens? {
        currentTokens?.let { return it }

        return withContext(ioDispatcher) {
            tokensMutex.withLock {
                currentTokens?.let { return@withContext it }

                val selectedUser = mutableUsersFlow.value?.find { it.isSelected }
                    ?: return@withContext null

                tokensDataSource()
                    .getCurrentUserTokens(selectedUser = selectedUser)
                    .also { currentTokens = it }
            }
        }
    }

    override suspend fun saveTokens(
        userId: String,
        accessToken: String,
        receivedAt: Instant,
        expiresIn: Int,
        refreshToken: String,
        scope: String,
        type: String,
    ) {
        withContext(ioDispatcher) {
            tokensDataSource()
                .saveTokens(
                    userId = userId,
                    accessToken = accessToken,
                    receivedAt = receivedAt,
                    expiresIn = expiresIn,
                    refreshToken = refreshToken,
                    scope = scope,
                    type = type,
                )
        }
    }
}
