package dev.yenny.calendar.auth.data.source.local

import dev.yenny.calendar.auth.data.model.GoogleTokensDb
import dev.yenny.calendar.auth.domain.model.GoogleTokens
import dev.yenny.calendar.auth.domain.model.GoogleUser
import dev.yenny.calendar.database.CalendarDatabase
import dev.yenny.calendar.di.invoke
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

internal interface TokensLocalDataSource {

    suspend fun getCurrentUserTokens(selectedUser: GoogleUser): GoogleTokens?

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

internal class TokensLocalDataSourceImpl(
    private val database: Lazy<CalendarDatabase>,
) : TokensLocalDataSource{

    override suspend fun getCurrentUserTokens(selectedUser: GoogleUser): GoogleTokens? {
        return database().read {
            GoogleTokensDb.selectAll()
                .where { GoogleTokensDb.userId eq selectedUser.id }
                .map { toDomainTokens(row = it) }
        }
            .firstOrNull()
    }

    override suspend fun saveTokens(
        userId: String,
        accessToken: String,
        receivedAt: Instant,
        expiresIn: Int,
        refreshToken: String,
        scope: String,
        type: String
    ) {
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
