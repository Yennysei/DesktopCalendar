package dev.yenny.calendar.httpclient

import dev.yenny.calendar.di.lazySafePublication
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

interface HttpClientComponent {

    val httpClient: Lazy<HttpClient>

    companion object {

        val instance: HttpClientComponent = HttpClientComponentImpl()
    }
}

private class HttpClientComponentImpl : HttpClientComponent {

    override val httpClient: Lazy<HttpClient> = lazySafePublication {
        HttpClient(engineFactory = OkHttp) {
            install(plugin = ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    }
                )
            }
        }
    }
}
