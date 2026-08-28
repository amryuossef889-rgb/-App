package com.example.ui.utils

import java.security.MessageDigest

object PasswordHasher {
    // Exact SHA-256 hash of "SunnahAdmin2026"
    private const val ADMIN_PASSWORD_HASH = "5b5135bec048142f8841627b2fd9d8b4181392df6ac1f77648606a048ef006ce"

    fun verifyPassword(input: String): Boolean {
        if (input.isBlank()) return false
        val hashed = hashString(input.trim())
        return hashed.equals(ADMIN_PASSWORD_HASH, ignoreCase = true)
    }

    private fun hashString(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
