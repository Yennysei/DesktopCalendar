package dev.yenny.calendar.database

import dev.yenny.calendar.di.unsafeReusable

interface DatabaseComponent {

    val database: Lazy<CalendarDatabase>

    companion object {

        val instance: DatabaseComponent = DatabaseComponentImpl()
    }
}

private class DatabaseComponentImpl : DatabaseComponent {

    override val database: Lazy<CalendarDatabase> = lazy {
        CalendarDatabaseImpl(
            passwordProvider = unsafeReusable { KeychainPasswordProviderImpl() },
        )
    }
}
