package de.sofoste.app.data.session

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.ktor.http.Cookie
import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SecureAdminCookieStorageTest {
    @Test
    fun adminCookieIsEncryptedScopedSeparatedAndClearable() = runBlocking {
        val testContext = InstrumentationRegistry.getInstrumentation().context
        val adminStorage = SecureAdminCookieStorage(testContext)
        val studentStorage = SecureStudentCookieStorage(testContext)
        val adminUrl = Url("https://sofoste.de/api/v1/admin/me")
        val sessionValue = "private-admin-session-for-test"

        adminStorage.clear()
        studentStorage.clear()
        adminStorage.addCookie(
            adminUrl,
            Cookie(
                name = "sofoste_admin_mobile",
                value = sessionValue,
                domain = "sofoste.de",
                path = "/",
                secure = true,
                httpOnly = true,
            ),
        )

        assertEquals(sessionValue, adminStorage.get(adminUrl).single().value)
        assertTrue(adminStorage.get(Url("https://example.com/api/v1/admin/me")).isEmpty())
        assertTrue(studentStorage.get(adminUrl).isEmpty())

        val encrypted = testContext
            .getSharedPreferences("sofoste_admin_mobile_session", 0)
            .getString("encrypted_cookie", null)
        assertFalse(encrypted.isNullOrBlank())
        assertFalse(encrypted.orEmpty().contains(sessionValue))

        adminStorage.clear()
        assertTrue(adminStorage.get(adminUrl).isEmpty())
    }
}
