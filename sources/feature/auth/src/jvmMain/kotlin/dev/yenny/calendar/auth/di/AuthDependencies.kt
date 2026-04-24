package dev.yenny.calendar.auth.di

import dev.yenny.calendar.database.CalendarDatabase
import dev.yenny.calendar.database.DatabaseComponent
import dev.yenny.calendar.httpclient.HttpClientComponent
import io.ktor.client.*

internal interface AuthDependencies {

    val database: Lazy<CalendarDatabase>
        get() = DatabaseComponent.instance.database

    val httpClient: Lazy<HttpClient>
        get() = HttpClientComponent.instance.httpClient

    companion object : AuthDependencies
}
