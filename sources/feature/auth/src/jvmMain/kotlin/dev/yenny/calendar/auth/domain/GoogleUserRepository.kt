package dev.yenny.calendar.auth.domain

import dev.yenny.calendar.auth.domain.model.GoogleTokens
import dev.yenny.calendar.auth.data.model.GoogleTokensDb
import dev.yenny.calendar.auth.domain.model.GoogleUser
import dev.yenny.calendar.auth.data.model.GoogleUserDb
import dev.yenny.calendar.database.CalendarDatabase
import dev.yenny.calendar.di.invoke
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.upsert
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

interface GoogleUserRepository {

    val usersFlow: Flow<List<GoogleUser>>

    suspend fun selectUser(user: GoogleUser)

    suspend fun getCurrentUserTokens(): GoogleTokens?

    suspend fun saveAndSelectUser(user: GoogleUser)

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
    private val database: Lazy<CalendarDatabase>,
    private val ioDispatcher: CoroutineDispatcher,
) : GoogleUserRepository {

    @Volatile
    private var currentTokens: GoogleTokens? = null

    private val tokensMutex: Mutex = Mutex()

    private val mutableUsersFlow: MutableStateFlow<List<GoogleUser>?> = MutableStateFlow(value = null)

    override val usersFlow: Flow<List<GoogleUser>> = flow {
        mutableUsersFlow.collect { users ->
            if (users == null) {
                mutableUsersFlow.value = getCachedUsers()
                return@collect
            }

            emit(value = users)
        }
    }

    override suspend fun selectUser(user: GoogleUser) {
        withContext(ioDispatcher) {
            val users = database().query {
                deselectAllUsers()

                GoogleUserDb.update(where = { GoogleUserDb.id eq user.id }) {
                    it[isSelected] = true
                }

                GoogleUserDb.selectAll()
                    .toDomainUsers()
            }

            currentTokens = null
            mutableUsersFlow.value = users
        }
    }

    override suspend fun getCurrentUserTokens(): GoogleTokens? {
        currentTokens?.let { return it }

        return withContext(ioDispatcher) {
            tokensMutex.withLock {
                currentTokens?.let { return@withContext it }

                val selectedUser = mutableUsersFlow.value?.find { it.isSelected } ?: return@withContext null

                database().read {
                    GoogleTokensDb.selectAll()
                        .where { GoogleTokensDb.userId eq selectedUser.id }
                        .map { toDomainTokens(row = it) }
                }
                    .firstOrNull()
                    .also { currentTokens = it }
            }
        }
    }

    override suspend fun saveAndSelectUser(user: GoogleUser) {
        withContext(ioDispatcher) {
            val users = database().query {
                deselectAllUsers()

                GoogleUserDb.upsert {
                    it[id] = user.id
                    it[name] = user.name
                    it[picture] = user.picture
                    it[givenName] = user.givenName
                    it[familyName] = user.familyName
                    it[isSelected] = true
                }

                GoogleUserDb.selectAll()
                    .toDomainUsers()
            }

            mutableUsersFlow.value = users
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
            val tokenExpiresAt = (receivedAt + expiresIn.seconds).toEpochMilliseconds()

            database().query {
                GoogleTokensDb.upsert {
                    it[this.userId] = userId
                    it[this.accessToken] = accessToken
                    it[this.expirationTimestamp] = tokenExpiresAt
                    it[this.refreshToken] = refreshToken
                    it[this.scope] = scope
                    it[this.tokenType] = type
                }
            }
        }
    }

    private suspend fun getCachedUsers(): List<GoogleUser> {
        return withContext(ioDispatcher) {
            database().read {
                GoogleUserDb.selectAll()
                    .map { toDomainUser(row = it) }
            }
        }
    }

    private fun deselectAllUsers() {
        GoogleUserDb.update(where = { GoogleUserDb.isSelected eq true }) {
            it[isSelected] = false
        }
    }

    private fun Query.toDomainUsers(): List<GoogleUser> {
        return map { toDomainUser(row = it) }
    }

    private fun toDomainUser(row: ResultRow): GoogleUser {
        return GoogleUser(
            id = row[GoogleUserDb.id],
            name = row[GoogleUserDb.name],
            picture = row[GoogleUserDb.picture],
            givenName = row[GoogleUserDb.givenName],
            familyName = row[GoogleUserDb.familyName],
            isSelected = row[GoogleUserDb.isSelected],
        )
    }

    private fun toDomainTokens(row: ResultRow): GoogleTokens {
        val expirationTimestamp = row[GoogleTokensDb.expirationTimestamp]

        return GoogleTokens(
            accessToken = row[GoogleTokensDb.accessToken],
            expirationInstant = Instant.fromEpochMilliseconds(expirationTimestamp),
            refreshToken = row[GoogleTokensDb.refreshToken],
            scope = row[GoogleTokensDb.scope],
            tokenType = row[GoogleTokensDb.tokenType],
        )
    }
}
