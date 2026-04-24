package dev.yenny.calendar.cryptography

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

fun getSha256(bytes: ByteArray): ByteArray {
    return MessageDigest.getInstance("SHA-256")
        .digest(bytes)
}

fun encryptBase64(bytes: ByteArray): String {
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(bytes)
}

class RandomGenerator(
    private val charPool: String = DEFAULT_CHAR_POOL,
    private val secureRandom: SecureRandom = SecureRandom(),
) {

    fun getRandomString(length: Int): String {
        return StringBuilder().apply {
            repeat(times = length) {
                val randomIndex = secureRandom.nextInt(charPool.length)
                val currentChar = charPool[randomIndex]
                append(currentChar)
            }
        }.toString()
    }

    companion object {

        const val DEFAULT_CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~"
    }
}
