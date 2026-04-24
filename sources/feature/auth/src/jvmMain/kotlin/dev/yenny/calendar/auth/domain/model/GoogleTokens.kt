package dev.yenny.calendar.auth.domain.model

import kotlin.time.Instant

data class GoogleTokens (
    val accessToken: String,
    val expirationInstant: Instant,
    val refreshToken: String,
    val scope: String,
    val tokenType: String,
)