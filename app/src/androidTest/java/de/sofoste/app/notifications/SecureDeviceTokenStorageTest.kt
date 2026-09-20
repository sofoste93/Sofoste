package de.sofoste.app.notifications

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SecureDeviceTokenStorageTest {
    @Test
    fun tokenIsEncryptedAndClearable() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val storage = SecureDeviceTokenStorage(context)
        val token = "a".repeat(64)
        storage.clear()
        storage.put(token)

        assertEquals(token, storage.get())
        val raw = context.getSharedPreferences("sofoste_triton_token", 0)
            .getString("encrypted_token", null).orEmpty()
        assertFalse(raw.contains(token))

        storage.clear()
        assertNull(storage.get())
    }

    @Test
    fun notificationIdentityUsesOnlyAFingerprint() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val preferences = TritonPreferences(context)
        val email = "learner@example.invalid"
        preferences.bindIdentity(email)

        assertEquals(true, preferences.matchesIdentity("LEARNER@example.invalid"))
        assertFalse(preferences.matchesIdentity("another@example.invalid"))
        val raw = context.getSharedPreferences("sofoste_triton_preferences", 0).all.values.joinToString()
        assertFalse(raw.contains(email))
    }
}
