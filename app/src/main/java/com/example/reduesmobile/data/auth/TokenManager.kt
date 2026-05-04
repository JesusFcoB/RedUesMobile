package com.example.reduesmobile.data.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.time.Instant

class TokenManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    fun saveToken(token: String, expiration: String) {
        prefs.edit().apply(){
            putString("jwt_token", token)
            putString("token_exp", expiration)
            apply()
        }
    }

    fun deleteToken() {
        prefs.edit().apply {
            remove("jwt_token")
            remove("token_exp")
            apply()
        }
    }


    fun getToken(): String? {
        return prefs.getString("jwt_token", null)
    }

    fun getExpiration(): String? {
        return prefs.getString("token_exp", null)
    }

    fun isValid(): Boolean {
        val token = getToken()
        val expiration = getExpiration()

        if (token == null || expiration == null) return false

        return try {
            val expirationDate = Instant.parse(expiration)
            val now = Instant.now()

            expirationDate.isAfter(now)
        } catch (e: Exception) {
            return false
        }

    }

    fun getUserId(): Int {
        val token = getToken() ?: return -1
        return try {
            val payload = token.split(".")[1]
            val decoded = android.util.Base64.decode(
                payload, android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING
            )
            val json = String(decoded)
            // El ID viene en el campo "sub"
            val regex = Regex(""""sub"\s*:\s*"?(\d+)"?""")
            val match = regex.find(json)
            match?.groupValues?.get(1)?.toInt() ?: -1
        } catch (e: Exception) {
            -1
        }
    }

    fun getUserName(): String? {
        val token = getToken() ?: return null
        return try {
            val payload = token.split(".")[1]
            val decoded = android.util.Base64.decode(
                payload, android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING
            )
            val json = String(decoded)
            val regex = Regex(""""unique_name"\s*:\s*"([^"]+)"""")
            val match = regex.find(json)
            match?.groupValues?.get(1)
        } catch (e: Exception) {
            null
        }
    }
}