package dev.yenny.calendar.database

import dev.yenny.calendar.di.invoke
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transactionManager
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

interface CalendarDatabase {

    suspend fun <T> query(
        transactionIsolation: Int? = null,
        statement: suspend JdbcTransaction.() -> T,
    ): T

    suspend fun <T> read(
        transactionIsolation: Int? = null,
        statement: suspend JdbcTransaction.() -> T,
    ): T
}

internal class CalendarDatabaseImpl(
    private val passwordProvider: Lazy<KeychainPasswordProvider>,
) : CalendarDatabase {

    private val database: Database by lazy { connectDatabase() }

    override suspend fun <T> query(
        transactionIsolation: Int?,
        statement: suspend JdbcTransaction.() -> T,
    ): T {
        return suspendTransaction(
            db = database,
            transactionIsolation = transactionIsolation ?: database.transactionManager.defaultIsolationLevel,
            readOnly = false,
            statement = statement,
        )
    }

    override suspend fun <T> read(
        transactionIsolation: Int?,
        statement: suspend JdbcTransaction.() -> T,
    ): T {
        return suspendTransaction(
            db = database,
            transactionIsolation = transactionIsolation ?: database.transactionManager.defaultIsolationLevel,
            readOnly = true,
            statement = statement,
        )
    }

    private fun connectDatabase(): Database {
        val fileDirPath = getAppDataDir()
            .resolve("DesktopCalendar")
            .also {  Files.createDirectories(it) }

        val password = passwordProvider().getPassword()
        val filePath = fileDirPath.resolve("database")

        return Database.connect(
            url = "jdbc:h2:file:$filePath;CIPHER=AES",
            driver = "org.h2.Driver",
            user = "DesktopCalendar",
            password = "$password $password",
        )
    }

    private fun getAppDataDir(): Path {
        val os = System.getProperty("os.name").lowercase()

        return when {
            os.contains("win") -> Paths.get(System.getenv("LOCALAPPDATA"))

            os.contains("mac") ->
                Paths.get(System.getProperty("user.home"), "Library", "Application Support")

            else -> {
                val dataHome = System.getenv("XDG_DATA_HOME")

                if (dataHome.isNullOrBlank()) {
                    Paths.get(System.getProperty("user.home"), ".local", "share")
                } else {
                    Paths.get(dataHome)
                }
            }
        }
    }
}
