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
}