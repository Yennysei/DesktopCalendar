package dev.yenny.calendar.auth.domain.model

data class GoogleUser(
    val id: String,
    val name: String?,
    val picture: String?,
    val givenName: String?,
    val familyName: String?,
    val isSelected: Boolean = false,
)