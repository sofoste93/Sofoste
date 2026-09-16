package de.sofoste.app.data.session

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.fillDefaults
import io.ktor.client.plugins.cookies.matches
import io.ktor.http.Cookie
import io.ktor.http.Url
import io.ktor.util.date.GMTDate
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SecureStudentCookieStorage(context: Context) : CookiesStorage {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun get(requestUrl: Url): List<Cookie> = mutex.withLock {
        val stored = read() ?: return@withLock emptyList()
        if (stored.expiresAt <= System.currentTimeMillis()) {
            clearLocked()
            return@withLock emptyList()
        }
        val cookie = Cookie(
            name = COOKIE_NAME,
            value = stored.value,
            expires = GMTDate(stored.expiresAt),
            domain = stored.domain,
            path = stored.path,
            secure = stored.secure,
            httpOnly = true,
        )
        if (cookie.matches(requestUrl)) listOf(cookie) else emptyList()
    }

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        if (cookie.name != COOKIE_NAME) return
        mutex.withLock {
            val now = System.currentTimeMillis()
            val normalized = cookie.fillDefaults(requestUrl)
            val serverExpiry = when {
                normalized.maxAge != null -> now + normalized.maxAge!!.coerceAtLeast(0) * 1_000L
                normalized.expires != null -> normalized.expires!!.timestamp
                else -> now + SESSION_LIFETIME_MS
            }
            val expiresAt = minOf(serverExpiry, now + SESSION_LIFETIME_MS)
            if (normalized.value.isEmpty() || expiresAt <= now) {
                clearLocked()
                return@withLock
            }
            write(
                StoredStudentCookie(
                    value = normalized.value,
                    domain = requireNotNull(normalized.domain),
                    path = requireNotNull(normalized.path),
                    secure = normalized.secure,
                    expiresAt = expiresAt,
                ),
            )
        }
    }

    suspend fun clear() {
        mutex.withLock { clearLocked() }
    }

    override fun close() = Unit

    private fun write(cookie: StoredStudentCookie) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val encrypted = cipher.doFinal(json.encodeToString(cookie).toByteArray(Charsets.UTF_8))
        val payload = Base64.encodeToString(cipher.iv, Base64.NO_WRAP) + "." +
            Base64.encodeToString(encrypted, Base64.NO_WRAP)
        preferences.edit { putString(PREFERENCE_COOKIE, payload) }
    }

    private fun read(): StoredStudentCookie? {
        val payload = preferences.getString(PREFERENCE_COOKIE, null) ?: return null
        return runCatching {
            val parts = payload.split('.', limit = 2)
            require(parts.size == 2)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val encrypted = Base64.decode(parts[1], Base64.NO_WRAP)
            cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(128, iv))
            json.decodeFromString<StoredStudentCookie>(
                cipher.doFinal(encrypted).toString(Charsets.UTF_8),
            )
        }.getOrElse {
            clearLocked()
            null
        }
    }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER).run {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build(),
            )
            generateKey()
        }
    }

    private fun clearLocked() {
        preferences.edit { remove(PREFERENCE_COOKIE) }
    }

    @Serializable
    private data class StoredStudentCookie(
        val value: String,
        val domain: String,
        val path: String,
        val secure: Boolean,
        val expiresAt: Long,
    )

    private companion object {
        const val COOKIE_NAME = "sofoste_student"
        const val PREFERENCES_NAME = "sofoste_student_session"
        const val PREFERENCE_COOKIE = "encrypted_cookie"
        const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        const val KEY_ALIAS = "sofoste_student_cookie_v1"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val SESSION_LIFETIME_MS = 8L * 60L * 60L * 1_000L
    }
}
