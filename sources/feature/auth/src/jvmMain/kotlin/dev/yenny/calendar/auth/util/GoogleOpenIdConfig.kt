package dev.yenny.calendar.auth.util

/**
 * [Google OpenID configuration](https://accounts.google.com/.well-known/openid-configuration)
 */
internal object GoogleOpenIdConfig {

    const val ISSUER: String = "https://accounts.google.com"
    const val AUTH_ENDPOINT: String = "https://accounts.google.com/o/oauth2/v2/auth"
    const val JWKS_URI: String = "https://www.googleapis.com/oauth2/v3/certs"
}