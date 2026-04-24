package dev.yenny.calendar.auth.data.model

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.SchemaUtils

internal object GoogleUserDb : Table(name = "google_user") {

    const val ID_LENGTH: Int = 128
    private const val NAME_MAX_VARCHAR_LENGTH: Int = 60

    val id: Column<String> = varchar(name = "id", length = ID_LENGTH)
    val name: Column<String?> = varchar(name = "name", length = 130).nullable()
    val picture: Column<String?> = varchar(name = "picture", length = 200).nullable()
    val givenName: Column<String?> = varchar(name = "given_name", length = NAME_MAX_VARCHAR_LENGTH).nullable()
    val familyName: Column<String?> = varchar(name = "family_name", length = NAME_MAX_VARCHAR_LENGTH).nullable()
    val isSelected: Column<Boolean> = bool(name = "is_selected_user").default(defaultValue = false)

    override val primaryKey: PrimaryKey = PrimaryKey(id)

    init {
        SchemaUtils.create(GoogleUserDb)
    }
}
