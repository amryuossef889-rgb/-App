package com.example

import com.example.ui.utils.PasswordHasher
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {
    @Test
    fun acceptsConfiguredAdminPassword() {
        assertTrue(PasswordHasher.verifyPassword("SunnahAdmin2026"))
    }

    @Test
    fun rejectsWrongAndBlankPasswords() {
        assertFalse(PasswordHasher.verifyPassword("wrong-password"))
        assertFalse(PasswordHasher.verifyPassword(""))
        assertFalse(PasswordHasher.verifyPassword("   "))
    }

    @Test
    fun trimsOuterWhitespace() {
        assertTrue(PasswordHasher.verifyPassword("  SunnahAdmin2026  "))
    }
}
