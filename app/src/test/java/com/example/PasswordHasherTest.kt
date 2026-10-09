package com.example

import android.content.Context
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.runner.RunWith
import com.example.ui.utils.PasswordHasher
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PasswordHasherTest {
    @Test
    fun firstUseCreatesPassphraseAndSubsequentUseVerifiesIt() {
        val context = RuntimeEnvironment.getApplication()
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
