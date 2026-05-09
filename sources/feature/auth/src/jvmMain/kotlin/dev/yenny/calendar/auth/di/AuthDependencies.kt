package dev.yenny.calendar.auth.di

import dev.yenny.calendar.database.CalendarDatabase
import dev.yenny.calendar.database.DatabaseComponent
import dev.yenny.calendar.httpclient.HttpClientComponent
import io.ktor.client.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal interface AuthDependencies {

    val ioDispatcher: CoroutineDispatcher
        get() = Dispatchers.IO

    val database: Lazy<CalendarDatabase>
        get() = DatabaseComponent.instance.database

    val httpClient: Lazy<HttpClient>
        get() = HttpClientComponent.instance.httpClient

    companion object : AuthDependencies
}
