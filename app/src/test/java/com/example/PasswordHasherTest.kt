package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.utils.PasswordHasher
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {
    @Test
    fun firstUseCreatesPassphraseAndSubsequentUseVerifiesIt() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("admin_gate_v2", Context.MODE_PRIVATE).edit().clear().commit()

        assertFalse(PasswordHasher.isConfigured(context))
        assertFalse(PasswordHasher.verifyOrCreate(context, "short"))
        assertTrue(PasswordHasher.verifyOrCreate(context, "local-passphrase-93"))
        assertTrue(PasswordHasher.isConfigured(context))
        assertTrue(PasswordHasher.verifyOrCreate(context, "local-passphrase-93"))
        assertFalse(PasswordHasher.verifyOrCreate(context, "wrong-passphrase"))

        context.getSharedPreferences("admin_gate_v2", Context.MODE_PRIVATE).edit().clear().commit()
    }
}
