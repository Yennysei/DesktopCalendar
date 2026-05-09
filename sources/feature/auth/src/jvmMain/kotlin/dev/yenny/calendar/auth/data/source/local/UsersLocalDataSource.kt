package dev.yenny.calendar.auth.data.source.local

import dev.yenny.calendar.auth.data.model.GoogleUserDb
import dev.yenny.calendar.auth.domain.model.GoogleUser
import dev.yenny.calendar.database.CalendarDatabase
import dev.yenny.calendar.di.invoke
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.upsert

internal interface UsersLocalDataSource {

    suspend fun getCachedUsers(): List<GoogleUser>

    suspend fun selectAndGetUsers(selectedUser: GoogleUser): List<GoogleUser>

    suspend fun saveAndSelectUser(user: GoogleUser): List<GoogleUser>
}

internal class UsersLocalDataSourceImpl(
    private val database: Lazy<CalendarDatabase>,
) : UsersLocalDataSource {

    override suspend fun getCachedUsers(): List<GoogleUser> {
        return database().read {
            GoogleUserDb.selectAll()
                .map { toDomainUser(row = it) }
        }
    }

    override suspend fun selectAndGetUsers(selectedUser: GoogleUser): List<GoogleUser> {
        return database().query {
            deselectAllUsers()

            GoogleUserDb.update(where = { GoogleUserDb.id eq selectedUser.id }) {
                it[isSelected] = true
            }

            GoogleUserDb.selectAll()
                .toDomainUsers()
        }
    }

    override suspend fun saveAndSelectUser(user: GoogleUser): List<GoogleUser> {
        return database().query {
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
}
