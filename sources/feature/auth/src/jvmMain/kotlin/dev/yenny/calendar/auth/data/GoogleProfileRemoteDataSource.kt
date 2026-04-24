package dev.yenny.calendar.auth.data

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.yenny.calendar.auth.util.GoogleOpenIdConfig
import dev.yenny.calendar.auth.data.model.GoogleCertificateResponse
import dev.yenny.calendar.auth.domain.model.GoogleUser
import dev.yenny.calendar.di.invoke
import dev.yenny.calendar.generated.Credentials
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.http.ContentType
import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
import java.util.Base64

internal interface GoogleProfileRemoteDataSource {

    suspend fun getUserProfile(idToken: String): GoogleUser
}

internal class GoogleProfileRemoteDataSourceImpl(
    private val client: Lazy<HttpClient>,
) : GoogleProfileRemoteDataSource {

    override suspend fun getUserProfile(idToken: String): GoogleUser {
        val certificatesResponse = client()
            .get(urlString = GoogleOpenIdConfig.JWKS_URI) {
                accept(ContentType.Application.Json)
            }
            .body<GoogleCertificateResponse>()

        val decodedJwt = JWT.decode(idToken)

        val certificate = decodedJwt.keyId
            ?.let { keyId -> certificatesResponse.keys.find { it.keyId == keyId } }

        checkNotNull(certificate) { "No certificate found for kid = ${decodedJwt.keyId}." }

        val modulus = getPublicKeyPart(string = certificate.rsaModulus)
        val exponent = getPublicKeyPart(string = certificate.rsaPublicExponent)
        val keySpec = RSAPublicKeySpec(modulus, exponent)

        val publicKey = KeyFactory.getInstance("RSA")
            .generatePublic(keySpec) as RSAPublicKey

        val algorithm = Algorithm.RSA256(publicKey, null)

        val verifier = JWT.require(algorithm)
            .withIssuer(GoogleOpenIdConfig.ISSUER)
            .withAudience(Credentials.CLIENT_ID)
            .build()

        val verifiedJwt = verifier.verify(decodedJwt)

        val claims = checkNotNull(verifiedJwt.claims) { "JWT's payload was not found." }
        val id = claims["sub"]?.asString()

        check(!id.isNullOrBlank()) {
            val idStatus = when {
                id == null -> "null"
                id.isEmpty() -> "empty"
                id.isBlank() -> "blank"
                else -> "unknown"
            }

            "JWT's 'sub' field is $idStatus."
        }

        val profile = GoogleUser(
            id = id,
            name = claims["name"]?.asString(),
            picture = claims["picture"]?.asString(),
            givenName = claims["given_name"]?.asString(),
            familyName = claims["family_name"]?.asString(),
        )

        return profile
    }

    private fun getPublicKeyPart(string: String): BigInteger {
        return BigInteger(1, decodeBase64(encoded = string))
    }

    private fun decodeBase64(encoded: String): ByteArray {
        return Base64.getUrlDecoder().decode(encoded)
    }
}
