package dev.yenny.calendar.auth.data.source.remote

import dev.yenny.calendar.auth.util.GoogleOpenIdConfig
import dev.yenny.calendar.auth.util.buildQueryParameters
import dev.yenny.calendar.cryptography.RandomGenerator
import dev.yenny.calendar.cryptography.encryptBase64
import dev.yenny.calendar.cryptography.getSha256
import dev.yenny.calendar.generated.Credentials
import io.ktor.http.ContentType
import io.ktor.server.application.install
import io.ktor.server.application.serverConfig
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.EngineConnectorBuilder
import io.ktor.server.engine.ShutDownUrl
import io.ktor.server.engine.applicationEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.response.respondText
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.awt.Desktop
import java.net.ServerSocket
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.coroutines.cancellation.CancellationException

internal interface GoogleAuthCodeRemoteDataSource {

    fun getAuthCodeFlow(): Flow<AuthCodeStatus>
}

sealed interface AuthCodeStatus {

    data object UrlOpen : AuthCodeStatus

    class Error(val throwable: Throwable) : AuthCodeStatus

    data class CodeReceived(
        val code: String,
        val redirectUri: String,
        val codeVerifier: String,
    ) : AuthCodeStatus
}

internal class GoogleAuthCodeRemoteDataSourceImpl : GoogleAuthCodeRemoteDataSource {

    @Volatile
    private var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>? = null

    private val serverMutex = Mutex()

    private val authCodeLogger: Logger = LoggerFactory.getLogger(GoogleAuthCodeRemoteDataSourceImpl::class.java)

    override fun getAuthCodeFlow(): Flow<AuthCodeStatus> = flow {
        val previousServer = server ?: serverMutex.withLock { server }
        previousServer?.stopSuspend()

        val port = ServerSocket(0).use {
            it.reuseAddress = true
            it.localPort
        }

        val redirectUri = "http://localhost:$port/"

        val randomStringGenerator = RandomGenerator()
        val state = randomStringGenerator.getRandomString(length = 30)
        val codeVerifierString = randomStringGenerator.getRandomString(length = 128)
        val channel = Channel<AuthCodeStatus>()

        val newServer = createLocalhostServer(
            port = port,
            state = state,
            redirectUri = redirectUri,
            codeVerifier = codeVerifierString,
            statusChannel = channel,
        )

        serverMutex.withLock { server = newServer }
        newServer.start(wait = false)

        val shaEncryptedCodeChallenge = getSha256(bytes = codeVerifierString.toByteArray())
        val base64EncryptedCodeChallenge = encryptBase64(bytes = shaEncryptedCodeChallenge)

        val query = getQueryParameters(
            state = state,
            scope = requestedScopes.joinToString(separator = " "),
            redirectUri = redirectUri,
            codeChallenge = base64EncryptedCodeChallenge,
        )

        val authUri = URI.create("${GoogleOpenIdConfig.AUTH_ENDPOINT}?$query")

        Desktop.getDesktop()
            .browse(authUri)

        emit(AuthCodeStatus.UrlOpen)

        val result = channel.receive()
        channel.close()
        emit(result)
    }
        .catch { emit(AuthCodeStatus.Error(it)) }

    private fun createLocalhostServer(
        port: Int,
        state: String,
        redirectUri: String,
        codeVerifier: String,
        statusChannel: Channel<AuthCodeStatus>,
    ): EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration> {
        val environment = applicationEnvironment { this.log = authCodeLogger }

        val applicationProperties = serverConfig(environment) {
            this.watchPaths = listOf(SystemFileSystem.resolve(Path(".")).toString())

            this.module {
                install(plugin = CallLogging) {
                    level = Level.INFO
                    logger = authCodeLogger
                }

                routing {
                    get(path = "/") {
                        val result = runCatching {
                            respond(state = state, redirectUri = redirectUri, codeVerifier = codeVerifier)
                        }
                            .onFailure { if (it is CancellationException) throw it }
                            .getOrElse { AuthCodeStatus.Error(throwable = it) }

                        statusChannel.send(result)
                    }
                }
            }
        }

        return embeddedServer(factory = Netty, rootConfig = applicationProperties) {
            val connector = EngineConnectorBuilder().apply {
                this.port = port
                this.host = "localhost"
            }

            this.connectors.add(connector)
        }
    }

    private suspend fun RoutingContext.respond(
        state: String,
        redirectUri: String,
        codeVerifier: String,
    ): AuthCodeStatus.CodeReceived {
        val error = call.queryParameters["error"]
        val resourceName = if (error == null) "success" else "error"
        val fullResourceName = "static/$resourceName.html"

        val html = this::class.java.classLoader
            .getResource(fullResourceName)
            ?.readText()

        call.respondText(html ?: "", ContentType.Text.Html)

        ShutDownUrl(url = "", exitCode = { 0 }, exit = {})
            .doShutdown(call = call)

        return if (error != null) {
            error(message = error)
        } else {
            handleCodeResponse(
                code = call.queryParameters["code"],
                scope = call.request.queryParameters["scope"],
                state = call.request.queryParameters["state"],
                expectedState = state,
                codeVerifier = codeVerifier,
                redirectUri = redirectUri,
            )
        }
    }

    private fun handleCodeResponse(
        code: String?,
        scope: String?,
        state: String?,
        expectedState: String,
        codeVerifier: String,
        redirectUri: String,
    ): AuthCodeStatus.CodeReceived {
        val grantedScopes = scope?.trim()
            ?.split(regex = "\\s".toRegex())

        when {
            code.isNullOrBlank() -> "Neither error nor code was found."
            grantedScopes.isNullOrEmpty() -> "No scopes provided."
            grantedScopes.none { OPEN_ID_SCOPE == it } -> "openid scope was not granted."
            grantedScopes.none { CALENDAR_SCOPE == it } -> "Calendar scope was not granted."
            state != expectedState -> "Received wrong state."
            else -> null
        }
            ?.let { error(message = it) }

        return AuthCodeStatus.CodeReceived(
            code = checkNotNull(code),
            redirectUri = redirectUri,
            codeVerifier = codeVerifier,
        )
    }

    private fun getQueryParameters(
        state: String,
        scope: String,
        redirectUri: String,
        codeChallenge: String,
    ): String {
        return buildQueryParameters(
            "client_id" to encodeQuery(Credentials.CLIENT_ID),
            "redirect_uri" to encodeQuery(redirectUri),
            "response_type" to "code",
            "scope" to encodeQuery(scope),
            "code_challenge" to encodeQuery(codeChallenge),
            "code_challenge_method" to "S256",
            "state" to encodeQuery(state),
        )
    }

    private fun encodeQuery(string: String): String {
        return URLEncoder.encode(string, StandardCharsets.UTF_8)
    }

    private companion object {

        const val OPEN_ID_SCOPE: String = "openid"
        const val PROFILE_SCOPE: String = "profile"
        const val CALENDAR_SCOPE: String = "https://www.googleapis.com/auth/calendar"

        val requestedScopes: List<String> = listOf(OPEN_ID_SCOPE, PROFILE_SCOPE, CALENDAR_SCOPE)
    }
}
