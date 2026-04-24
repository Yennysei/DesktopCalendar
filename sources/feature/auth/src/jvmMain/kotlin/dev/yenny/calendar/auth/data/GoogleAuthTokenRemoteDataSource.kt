package dev.yenny.calendar.auth.data

import dev.yenny.calendar.auth.data.model.TokensApiResponse
import dev.yenny.calendar.auth.util.buildQueryParameters
import dev.yenny.calendar.di.invoke
import dev.yenny.calendar.generated.Credentials
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.withCharset
import io.ktor.utils.io.charsets.Charset

internal interface GoogleAuthTokenRemoteDataSource {

    suspend fun getAuthToken(
        code: String,
        redirectUri: String,
        codeVerifier: String,
    ): TokensApiResponse.Success
}

internal class GoogleAuthTokenRemoteDataSourceImpl(
    private val client: Lazy<HttpClient>,
) : GoogleAuthTokenRemoteDataSource {

    override suspend fun getAuthToken(
        code: String,
        redirectUri: String,
        codeVerifier: String,
    ): TokensApiResponse.Success {
        val body = buildQueryParameters(
            "client_id" to Credentials.CLIENT_ID,
            "client_secret" to Credentials.CLIENT_SECRET,
            "code" to code,
            "codeVerifier" to codeVerifier,
            "grant_type" to "authorization_code",
            "redirect_uri" to redirectUri,
        )

        val request = client()
            .post(urlString = "https://oauth2.googleapis.com/token") {
                contentType(ContentType.Application.FormUrlEncoded.withCharset(charset = Charsets.UTF_8))
                accept(ContentType.parse("Accept=text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"))
                setBody(body = body)
                header(HttpHeaders.ContentLength, body.toByteArray(charset = Charset.forName("ASCII")).size)
            }

        return when (val response = request.call.body<TokensApiResponse>()) {
            is TokensApiResponse.Error -> {
                // TODO log
                val message = StringBuilder("Error token response:")
                    .appendLine()
                    .append("error = ")
                    .append(response.error)
                    .appendLine()
                    .append("errorDescription = ")
                    .append(response.errorDescription)
                    .toString()

                error(message = message)
            }

            is TokensApiResponse.Success -> response
        }
    }
}
