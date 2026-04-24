package dev.yenny.calendar.auth.data.model

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.SchemaUtils

/**
 * [Token size](https://developers.google.com/identity/protocols/oauth2#size)
 */
internal object GoogleTokensDb : Table(name = "google_tokens") {

    val userId: Column<String> = varchar(name = "user_id", length = GoogleUserDb.ID_LENGTH)
        .references(GoogleUserDb.id)

    val accessToken: Column<String> = varchar(name = "access_token", length = 2048)
    val expirationTimestamp: Column<Long> = long(name = "expiration_timestamp")
    val refreshToken: Column<String> = varchar(name = "refresh_token", length = 512)
    val scope: Column<String> = varchar(name = "scope", length = 2048)
    val tokenType: Column<String> = varchar(name = "token_type", length = 32)

    override val primaryKey: PrimaryKey = PrimaryKey(userId)

    init {
        SchemaUtils.create(GoogleTokensDb)
    }
}
