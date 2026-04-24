package dev.yenny.calendar.database

import com.github.javakeyring.Keyring
import com.github.javakeyring.PasswordAccessException
import java.security.SecureRandom
import java.util.Base64

internal interface KeychainPasswordProvider {

    fun getPassword(): String
}

internal class KeychainPasswordProviderImpl : KeychainPasswordProvider {

    override fun getPassword(): String {
        Keyring.create().use { keyring ->
            val username = System.getProperty("user.name")

            val existing = try {
                keyring.getPassword(SERVICE_KEY, username)
            } catch (_ : PasswordAccessException) {
                null
            }

            if (existing != null) return existing

            val newPassword = generateSecurePassword()
            keyring.setPassword(SERVICE_KEY, username, newPassword)

            return newPassword
        }
    }

    private fun generateSecurePassword(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }

    private companion object {

        const val SERVICE_KEY: String = "DesktopCalendar"
    }
}
