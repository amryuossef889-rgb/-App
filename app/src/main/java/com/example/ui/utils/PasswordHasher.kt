package com.example.ui.utils

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Local-only gate for the library editor.
 *
 * No credential is shipped in the APK. The first use lets the device owner
 * create a passphrase; a salted PBKDF2 verifier is kept in app-private prefs.
 * This is a UI privacy gate, not server-grade authorization against a person
 * who can modify/repackage the APK or access a rooted device.
 */
object PasswordHasher {
    private const val PREFS = "admin_gate_v2"
    private const val KEY_SALT = "salt"
    private const val KEY_HASH = "verifier"
    private const val ITERATIONS = 210_000
    private const val KEY_BITS = 256
    private const val MIN_LENGTH = 8

    fun isConfigured(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return !prefs.getString(KEY_SALT, null).isNullOrBlank() &&
            !prefs.getString(KEY_HASH, null).isNullOrBlank()
    }

    fun verifyOrCreate(context: Context, input: String): Boolean {
        val password = input.toCharArray()
        if (password.size < MIN_LENGTH || input.isBlank()) return false
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val storedSalt = prefs.getString(KEY_SALT, null)
        val storedHash = prefs.getString(KEY_HASH, null)

        if (storedSalt == null || storedHash == null) {
            val salt = ByteArray(16).also(SecureRandom()::nextBytes)
            val verifier = derive(password, salt)
            return prefs.edit()
                .putString(KEY_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
                .putString(KEY_HASH, Base64.encodeToString(verifier, Base64.NO_WRAP))
                .commit()
        }

        return try {
            val salt = Base64.decode(storedSalt, Base64.NO_WRAP)
            val expected = Base64.decode(storedHash, Base64.NO_WRAP)
            MessageDigest.isEqual(expected, derive(password, salt))
        } catch (_: IllegalArgumentException) {
            false
        } finally {
            password.fill('\u0000')
        }
    }

    private fun derive(password: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }
}
