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
class SecureStudentCookieStorageTest {
    @Test
    fun studentCookieIsEncryptedScopedAndClearable() = runBlocking {
        val testContext = InstrumentationRegistry.getInstrumentation().context
        val storage = SecureStudentCookieStorage(testContext)
        val studentUrl = Url("https://sofoste.de/api/v1/student/me")
        val sessionValue = "private-session-value-for-test"

        storage.clear()
        storage.addCookie(
            studentUrl,
            Cookie(
                name = "sofoste_student",
                value = sessionValue,
                domain = "sofoste.de",
                path = "/",
                secure = true,
                httpOnly = true,
            ),
        )

        assertEquals(sessionValue, storage.get(studentUrl).single().value)
        assertTrue(storage.get(Url("https://example.com/api/v1/student/me")).isEmpty())

        val encrypted = testContext
            .getSharedPreferences("sofoste_student_session", 0)
            .getString("encrypted_cookie", null)
        assertFalse(encrypted.isNullOrBlank())
        assertFalse(encrypted.orEmpty().contains(sessionValue))

        storage.clear()
        assertTrue(storage.get(studentUrl).isEmpty())
    }
}
